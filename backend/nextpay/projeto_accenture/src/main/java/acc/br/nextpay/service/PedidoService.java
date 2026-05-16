package acc.br.nextpay.service;

import acc.br.nextpay.dto.PedidoDTO;
import acc.br.nextpay.model.ContaCorrente;
import acc.br.nextpay.model.Pedido;
import acc.br.nextpay.model.Produto;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.model.enums.NivelUsuario;
import acc.br.nextpay.model.enums.StatusPedido;
import acc.br.nextpay.model.enums.TipoTransacao;
import acc.br.nextpay.repository.ContaCorrenteRepository;

import acc.br.nextpay.repository.PedidoRepository;
import acc.br.nextpay.repository.ProdutoRepository;
import acc.br.nextpay.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ContaCorrenteRepository contaCorrenteRepository;
    private final TransacaoService transacaoService;

    @Autowired
    private EmailService emailService;

    @Transactional
    public PedidoDTO comprar(Long compradorId, Long produtoId, Integer quantidade, BigDecimal moedasUsadas) {
        Usuario comprador = buscarUsuario(compradorId);
        Produto produto = buscarProduto(produtoId);

        validarPedido(comprador, produto, quantidade);

        Usuario vendedor = produto.getVendedor();
        BigDecimal valorTotal = calcularValorTotal(produto, quantidade);

        realizarPagamento(comprador, vendedor, valorTotal, produto.getNome(), moedasUsadas);

        produto.baixarEstoque(quantidade);
        produtoRepository.save(produto);

        // // Atribuir pontuação
        // comprador.adicionarPontuacao(1);
        // usuarioRepository.saveAndFlush(comprador); // Alterado para saveAndFlush

        // vendedor.adicionarPontuacao(1);
        // usuarioRepository.saveAndFlush(vendedor); // Alterado para saveAndFlush

        Pedido pedido = salvarPedido(comprador, vendedor, produto, quantidade, valorTotal, StatusPedido.FINALIZADO);

        return montarDTO(pedido);
    }

    @Transactional
    public PedidoDTO reservar(Long compradorId, Long produtoId, Integer quantidade) {
        Usuario comprador = buscarUsuario(compradorId);
        Produto produto = buscarProduto(produtoId);

        validarPedido(comprador, produto, quantidade);

        Usuario vendedor = produto.getVendedor();
        BigDecimal valorTotal = calcularValorTotal(produto, quantidade);

        produto.baixarEstoque(quantidade);
        produtoRepository.save(produto);

        Pedido pedido = salvarPedido(comprador, vendedor, produto, quantidade, valorTotal, StatusPedido.RESERVADO);

        return montarDTO(pedido);
    }

    @Transactional
    public PedidoDTO confirmarReserva(Long pedidoId, Long usuarioId, BigDecimal moedasUsadas) {
        Pedido pedido = buscarPedido(pedidoId);

        if (pedido.getStatus() != StatusPedido.RESERVADO) {
            throw new RuntimeException("Apenas pedidos reservados podem ser confirmados.");
        }

        realizarPagamento(pedido.getComprador(), pedido.getVendedor(), pedido.getValorTotal(),
                pedido.getProduto().getNome(), moedasUsadas);

        // realizarPagamento(pedido.getComprador(), pedido.getVendedor(),
        // pedido.getValorTotal(),
        // pedido.getProduto().getNome());

        // // Atribuir pontuação na confirmação da reserva
        Usuario comprador = pedido.getComprador();
        Usuario vendedor = pedido.getVendedor();

        NivelUsuario nivelAnteriorComprador = comprador.getNivel();
        NivelUsuario nivelAnteriorVendedor = vendedor.getNivel();

        // comprador.adicionarPontuacao(1);
        // vendedor.adicionarPontuacao(1);

        usuarioRepository.saveAndFlush(comprador);
        usuarioRepository.saveAndFlush(vendedor);

        notificarMudancaDeNivel(comprador, nivelAnteriorComprador);
        notificarMudancaDeNivel(vendedor, nivelAnteriorVendedor); // Alterado para saveAndFlush

        pedido.setStatus(StatusPedido.FINALIZADO);

        try {
            emailService.enviarEmail(
                    vendedor.getEmail(),
                    "Produto vendido - NextPay",
                    "Olá, " + vendedor.getNome() + "!\n\n" +
                            "Seu produto foi vendido com sucesso no marketplace.\n\n" +
                            "Produto: " + pedido.getProduto().getNome() + "\n" +
                            "Valor da venda: R$ " + pedido.getValorTotal() + "\n\n" +
                            "O valor já foi transferido para sua conta.");
        } catch (Exception e) {
            System.out.println("Erro ao enviar e-mail de venda: " + e.getMessage());
        }

        return montarDTO(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoDTO cancelarReserva(Long pedidoId) {
        Pedido pedido = buscarPedido(pedidoId);

        if (pedido.getStatus() != StatusPedido.RESERVADO) {
            throw new RuntimeException("Apenas pedidos reservados podem ser cancelados.");
        }

        pedido.getProduto().devolverEstoque(pedido.getQuantidade());
        produtoRepository.save(pedido.getProduto());

        pedido.setStatus(StatusPedido.CANCELADO);

        return montarDTO(pedidoRepository.save(pedido));
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }

    private Produto buscarProduto(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));
    }

    private Pedido buscarPedido(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));
    }

    private void validarPedido(Usuario comprador, Produto produto, Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new RuntimeException("A quantidade deve ser maior que zero.");
        }

        if (produto.getVendedor().getId().equals(comprador.getId())) {
            throw new RuntimeException("Você não pode comprar ou reservar seu próprio produto.");
        }

        if (!produto.temEstoqueDisponivel(quantidade)) {
            throw new RuntimeException("Estoque insuficiente.");
        }
    }

    private BigDecimal calcularValorTotal(Produto produto, Integer quantidade) {
        return produto.getPreco().multiply(BigDecimal.valueOf(quantidade));
    }

    private void realizarPagamento(Usuario comprador, Usuario vendedor, BigDecimal valorTotal, String nomeProduto,
            BigDecimal moedasUsadas) {
        if (moedasUsadas == null)
            moedasUsadas = BigDecimal.ZERO;

        if (BigDecimal.valueOf(comprador.getPontuacao()).compareTo(moedasUsadas) < 0) {
            // throw new NegocioException("Saldo de moedas insuficiente.");
        }

        // Proporção: 1 moeda = 0.10 RS (10 moedas = 1 RS)
        BigDecimal valorAbatimento = moedasUsadas.divide(BigDecimal.valueOf(10), 2, java.math.RoundingMode.HALF_UP);

        BigDecimal valorAPagarEmDinheiro = valorTotal.subtract(valorAbatimento);
        if (valorAPagarEmDinheiro.compareTo(BigDecimal.ZERO) < 0) {
            valorAPagarEmDinheiro = BigDecimal.ZERO;
            // Se o abatimento for maior que o total, ajustamos as moedas usadas
            moedasUsadas = valorTotal.multiply(BigDecimal.valueOf(10));
        }

        Long idContaComprador = comprador.getConta().getId();
        Long idContaVendedor = vendedor.getConta().getId();

        ContaCorrente contaA = contaCorrenteRepository.findByIdWithLock(Math.min(idContaComprador, idContaVendedor));
        // .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não
        // encontrada."));
        ContaCorrente contaB = contaCorrenteRepository.findByIdWithLock(Math.max(idContaComprador, idContaVendedor));
        // .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não
        // encontrada."));

        ContaCorrente contaComprador = contaA.getId().equals(idContaComprador) ? contaA : contaB;
        ContaCorrente contaVendedor = contaA.getId().equals(idContaVendedor) ? contaA : contaB;

        if (!contaComprador.possuiSaldoSuficiente(valorAPagarEmDinheiro)) {
            // throw new SaldoInsuficienteException("Saldo insuficiente para concluir a
            // compra.");
        }

        // Deduções e Recompensas de Moedas
        int moedasParaDeduzir = moedasUsadas.intValue();

        // Recompensa baseada no nível: coinsRecebidas = valorDaCompra *
        // percentualCashback * 10
        double percentual = comprador.getNivel().getPercentualCashback();
        int cashbackMoedas = valorTotal.multiply(BigDecimal.valueOf(percentual))
                .intValue();

        // Atualização de Saldo (Coins/Pontuação)
        comprador.setPontuacao(comprador.getPontuacao() - moedasParaDeduzir + cashbackMoedas);

        // Incremento de Transações (que define o Nível)
        comprador.adicionarTransacao();
        vendedor.adicionarTransacao();

        contaComprador.setSaldo(contaComprador.getSaldo().subtract(valorAPagarEmDinheiro));
        contaVendedor.setSaldo(contaVendedor.getSaldo().add(valorTotal));

        contaCorrenteRepository.save(contaComprador);
        contaCorrenteRepository.save(contaVendedor);
        usuarioRepository.save(comprador);
        usuarioRepository.save(vendedor);

        transacaoService.registrarTransacao(comprador, valorAPagarEmDinheiro, TipoTransacao.COMPRA,
                "Compra do produto: " + nomeProduto + " (Abatido " + moedasUsadas + " moedas = R$ " + valorAbatimento
                        + ")");
        transacaoService.registrarTransacao(vendedor, valorTotal, TipoTransacao.VENDA,
                "Venda do produto: " + nomeProduto);
    }

    private void notificarMudancaDeNivel(Usuario usuario, NivelUsuario nivelAnterior) {
        NivelUsuario nivelAtual = usuario.getNivel();

        if (nivelAtual == null || nivelAnterior == null) {
            return;
        }

        if (!nivelAtual.equals(nivelAnterior)) {
            try {
                emailService.enviarEmail(
                        usuario.getEmail(),
                        "Parabéns! Você subiu de nível no NextPay",
                        "Olá, " + usuario.getNome() + "!\n\n" +
                                "Sua conta no marketplace foi promovida para o nível " + nivelAtual + ".\n\n" +
                                "Continue comprando e vendendo para alcançar novos benefícios.");
            } catch (Exception e) {
                System.out.println("Erro ao enviar e-mail de mudança de nível: " + e.getMessage());
            }
        }
    }

    private Pedido salvarPedido(Usuario comprador, Usuario vendedor, Produto produto,
            Integer quantidade, BigDecimal valorTotal, StatusPedido status) {

        Pedido pedido = Pedido.builder()
                .comprador(comprador)
                .vendedor(vendedor)
                .produto(produto)
                .quantidade(quantidade)
                .valorTotal(valorTotal)
                .status(status)
                .dataPedido(LocalDateTime.now())
                .build();

        return pedidoRepository.save(pedido);
    }

    private PedidoDTO montarDTO(Pedido pedido) {
        return PedidoDTO.builder()
                .id(pedido.getId())
                .nomeComprador(pedido.getComprador().getNome())
                .nomeVendedor(pedido.getVendedor().getNome())
                .nomeProduto(pedido.getProduto().getNome())
                .quantidade(pedido.getQuantidade())
                .valorTotal(pedido.getValorTotal())
                .dataPedido(pedido.getDataPedido())
                .status(pedido.getStatus().toString())
                .build();
    }
}