package acc.br.nextpay.service;

import acc.br.nextpay.model.ContaCorrente;
import acc.br.nextpay.model.Transacao;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.model.enums.TipoTransacao;
import acc.br.nextpay.repository.ContaCorrenteRepository;
import acc.br.nextpay.repository.TransacaoRepository;
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
public class TransacaoService {

    private final UsuarioRepository usuarioRepository;
    private final ContaCorrenteRepository contaCorrenteRepository;
    private final TransacaoRepository transacaoRepository;

    public List<Transacao> listarTransacoesPorUsuario(Long usuarioId) {
        return transacaoRepository.findByUsuarioIdOrderByDataDesc(usuarioId);
    }

    public Usuario buscarUsuarioPorChavePix(String chavePix) {
        return usuarioRepository.findByEmail(chavePix)
            .or(() -> usuarioRepository.findByCpfCnpj(chavePix))
            .orElseThrow(() -> new RuntimeException("Chave PIX não encontrada."));
    }

    @Autowired
    private EmailService emailService;

    @Transactional
    public ContaCorrente depositar(Long usuarioId, BigDecimal valor) {
        validarValor(valor);

        Usuario usuario = buscarUsuario(usuarioId);
        ContaCorrente conta = usuario.getConta();

        conta.setSaldo(conta.getSaldo().add(valor));
        contaCorrenteRepository.save(conta);

        registrarTransacao(usuario, valor, TipoTransacao.DEPOSITO, "Depósito em conta");

        return conta;
    }

    @Transactional
    public ContaCorrente sacar(Long usuarioId, BigDecimal valor) {
        validarValor(valor);

        Usuario usuario = buscarUsuario(usuarioId);
        ContaCorrente conta = usuario.getConta();

        if (!conta.possuiSaldoSuficiente(valor)) {
            throw new RuntimeException("Saldo insuficiente para saque (incluindo limite).");
        }

        conta.setSaldo(conta.getSaldo().subtract(valor));
        contaCorrenteRepository.save(conta);

        registrarTransacao(usuario, valor, TipoTransacao.SAQUE, "Saque em conta");

        return conta;
    }

    @Transactional
    public String pix(Long usuarioOrigemId, String chavePix, BigDecimal valor) {
        validarValor(valor);

        Usuario origem = buscarUsuario(usuarioOrigemId);
        Usuario destino = buscarUsuarioPorChavePix(chavePix);

        if (origem.getId().equals(destino.getId())) {
            throw new RuntimeException("Não é possível fazer PIX para a própria conta.");
        }

        ContaCorrente contaOrigem = origem.getConta();
        ContaCorrente contaDestino = destino.getConta();

        if (!contaOrigem.possuiSaldoSuficiente(valor)) {
            throw new RuntimeException("Saldo insuficiente para PIX.");
        }

        contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(valor));
        contaDestino.setSaldo(contaDestino.getSaldo().add(valor));

        contaCorrenteRepository.save(contaOrigem);
        contaCorrenteRepository.save(contaDestino);

        registrarTransacao(origem, valor, TipoTransacao.PIX, "PIX enviado para " + destino.getNome());
        registrarTransacao(destino, valor, TipoTransacao.PIX, "PIX recebido de " + origem.getNome());

        try {
            emailService.enviarEmail(
                    destino.getEmail(),
                    "Você recebeu um PIX - NextPay",
                    "Olá, " + destino.getNome() + "!\n\n" +
                    "Você recebeu um PIX de R$ " + valor + " enviado por " + origem.getNome() + ".\n\n" +
                    "Acesse sua conta NextPay para visualizar a movimentação."
            );
        } catch (Exception e) {
            System.out.println("Erro ao enviar e-mail de PIX recebido: " + e.getMessage());
        }

        return "PIX realizado com sucesso.";
    }

    @Transactional
    public String transferir(Long usuarioOrigemId, Long usuarioDestinoId, BigDecimal valor) {
        validarValor(valor);

        if (usuarioOrigemId.equals(usuarioDestinoId)) {
            throw new RuntimeException("Não é possível fazer transferência para a própria conta.");
        }

        Usuario origem = buscarUsuario(usuarioOrigemId);
        Usuario destino = buscarUsuario(usuarioDestinoId);

        ContaCorrente contaOrigem = origem.getConta();
        ContaCorrente contaDestino = destino.getConta();

        if (!contaOrigem.possuiSaldoSuficiente(valor)) {
            throw new RuntimeException("Saldo insuficiente para transferência (incluindo limite).");
        }

        contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(valor));
        contaDestino.setSaldo(contaDestino.getSaldo().add(valor));

        contaCorrenteRepository.save(contaOrigem);
        contaCorrenteRepository.save(contaDestino);

        registrarTransacao(origem, valor, TipoTransacao.TRANSFERENCIA, "Transferência enviada para " + destino.getNome());
        registrarTransacao(destino, valor, TipoTransacao.TRANSFERENCIA, "Transferência recebida de " + origem.getNome());

        return "Transferência realizada com sucesso.";
    }

    @Transactional
    public void registrarTransacao(Usuario usuario, BigDecimal valor, TipoTransacao tipo, String descricao) {
        Transacao transacao = Transacao.builder()
                .usuario(usuario)
                .valor(valor)
                .tipo(tipo)
                .descricao(descricao)
                .data(LocalDateTime.now())
                .build();
        transacaoRepository.save(transacao);
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }

    private void validarValor(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("O valor deve ser maior que zero.");
        }
    }
}
