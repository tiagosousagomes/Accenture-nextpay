package acc.br.nextpay.service;

import acc.br.nextpay.dto.PedidoDTO;
import acc.br.nextpay.exception.NegocioException;
import acc.br.nextpay.exception.RecursoNaoEncontradoException;
import acc.br.nextpay.exception.SaldoInsuficienteException;
import acc.br.nextpay.model.ContaCorrente;
import acc.br.nextpay.model.Pedido;
import acc.br.nextpay.model.Produto;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.model.enums.StatusPedido;
import acc.br.nextpay.model.enums.TipoTransacao;
import acc.br.nextpay.repository.ContaCorrenteRepository;
import acc.br.nextpay.repository.PedidoRepository;
import acc.br.nextpay.repository.ProdutoRepository;
import acc.br.nextpay.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public PedidoDTO comprar(Long compradorId, Long produtoId, Integer quantidade) {
        Usuario comprador = buscarUsuario(compradorId);
        Produto produto = buscarProduto(produtoId);

        validarPedido(comprador, produto, quantidade);

        Usuario vendedor = produto.getVendedor();
        BigDecimal valorTotal = calcularValorTotal(produto, quantidade);

        realizarPagamento(comprador, vendedor, valorTotal, produto.getNome());

        produto.baixarEstoque(quantidade);
        produtoRepository.save(produto);

        comprador.adicionarPontuacao(1);
        usuarioRepository.saveAndFlush(comprador);

        vendedor.adicionarPontuacao(1);
        usuarioRepository.saveAndFlush(vendedor);

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
    public PedidoDTO confirmarReserva(Long pedidoId, Long usuarioId) {
        Pedido pedido = buscarPedido(pedidoId);

        if (!pedido.getComprador().getId().equals(usuarioId)) {
            throw new NegocioException("Apenas o comprador pode confirmar esta reserva.");
        }

        if (pedido.getStatus() != StatusPedido.RESERVADO) {
            throw new NegocioException("Apenas pedidos reservados podem ser confirmados.");
        }

        realizarPagamento(pedido.getComprador(), pedido.getVendedor(), pedido.getValorTotal(), pedido.getProduto().getNome());

        Usuario comprador = pedido.getComprador();
        Usuario vendedor = pedido.getVendedor();
        comprador.adicionarPontuacao(1);
        vendedor.adicionarPontuacao(1);
        usuarioRepository.saveAndFlush(comprador);
        usuarioRepository.saveAndFlush(vendedor);

        pedido.setStatus(StatusPedido.FINALIZADO);

        return montarDTO(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoDTO cancelarReserva(Long pedidoId, Long usuarioId) {
        Pedido pedido = buscarPedido(pedidoId);

        if (!pedido.getComprador().getId().equals(usuarioId)) {
            throw new NegocioException("Apenas o comprador pode cancelar esta reserva.");
        }

        if (pedido.getStatus() != StatusPedido.RESERVADO) {
            throw new NegocioException("Apenas pedidos reservados podem ser cancelados.");
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
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    private Produto buscarProduto(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));
    }

    private Pedido buscarPedido(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));
    }

    private void validarPedido(Usuario comprador, Produto produto, Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new NegocioException("A quantidade deve ser maior que zero.");
        }

        if (produto.getVendedor().getId().equals(comprador.getId())) {
            throw new NegocioException("Você não pode comprar ou reservar seu próprio produto.");
        }

        if (!produto.temEstoqueDisponivel(quantidade)) {
            throw new NegocioException("Estoque insuficiente.");
        }
    }

    private BigDecimal calcularValorTotal(Produto produto, Integer quantidade) {
        return produto.getPreco().multiply(BigDecimal.valueOf(quantidade));
    }

    private void realizarPagamento(Usuario comprador, Usuario vendedor, BigDecimal valorTotal, String nomeProduto) {
        Long idContaComprador = comprador.getConta().getId();
        Long idContaVendedor = vendedor.getConta().getId();

        ContaCorrente contaA = contaCorrenteRepository.findByIdWithLock(Math.min(idContaComprador, idContaVendedor))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada."));
        ContaCorrente contaB = contaCorrenteRepository.findByIdWithLock(Math.max(idContaComprador, idContaVendedor))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada."));

        ContaCorrente contaComprador = contaA.getId().equals(idContaComprador) ? contaA : contaB;
        ContaCorrente contaVendedor = contaA.getId().equals(idContaVendedor) ? contaA : contaB;

        if (!contaComprador.possuiSaldoSuficiente(valorTotal)) {
            throw new SaldoInsuficienteException("Saldo insuficiente para concluir a compra.");
        }

        contaComprador.setSaldo(contaComprador.getSaldo().subtract(valorTotal));
        contaVendedor.setSaldo(contaVendedor.getSaldo().add(valorTotal));

        contaCorrenteRepository.save(contaComprador);
        contaCorrenteRepository.save(contaVendedor);

        transacaoService.registrarTransacao(comprador, valorTotal, TipoTransacao.COMPRA, "Compra do produto: " + nomeProduto);
        transacaoService.registrarTransacao(vendedor, valorTotal, TipoTransacao.VENDA, "Venda do produto: " + nomeProduto);
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
