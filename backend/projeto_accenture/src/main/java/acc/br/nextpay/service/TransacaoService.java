package acc.br.nextpay.service;

import acc.br.nextpay.exception.NegocioException;
import acc.br.nextpay.exception.RecursoNaoEncontradoException;
import acc.br.nextpay.exception.SaldoInsuficienteException;
import acc.br.nextpay.model.ContaCorrente;
import acc.br.nextpay.model.Transacao;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.model.enums.TipoTransacao;
import acc.br.nextpay.repository.ContaCorrenteRepository;
import acc.br.nextpay.repository.TransacaoRepository;
import acc.br.nextpay.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
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

    public ContaCorrente buscarContaPorUsuario(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        return usuario.getConta();
    }

    public Usuario buscarUsuarioPorChavePix(String chavePix) {
        return usuarioRepository.findByEmail(chavePix)
                .or(() -> usuarioRepository.findByCpfCnpj(chavePix))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chave PIX não encontrada."));
    }

    @Transactional
    public ContaCorrente depositar(Long usuarioId, BigDecimal valor) {
        validarValor(valor);

        Usuario usuario = buscarUsuario(usuarioId);
        ContaCorrente conta = bloquearConta(usuario.getConta().getId());

        conta.setSaldo(conta.getSaldo().add(valor));
        contaCorrenteRepository.save(conta);

        registrarTransacao(usuario, valor, TipoTransacao.DEPOSITO, "Depósito em conta");

        return conta;
    }

    @Transactional
    public ContaCorrente sacar(Long usuarioId, BigDecimal valor) {
        validarValor(valor);

        Usuario usuario = buscarUsuario(usuarioId);
        ContaCorrente conta = bloquearConta(usuario.getConta().getId());

        if (!conta.possuiSaldoSuficiente(valor)) {
            throw new SaldoInsuficienteException("Saldo insuficiente para saque.");
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
            throw new NegocioException("Não é possível fazer PIX para a própria conta.");
        }

        Long idContaOrigem = origem.getConta().getId();
        Long idContaDestino = destino.getConta().getId();

        // Lock em ordem crescente de ID para evitar deadlock
        ContaCorrente contaA = bloquearConta(Math.min(idContaOrigem, idContaDestino));
        ContaCorrente contaB = bloquearConta(Math.max(idContaOrigem, idContaDestino));

        ContaCorrente contaOrigem = contaA.getId().equals(idContaOrigem) ? contaA : contaB;
        ContaCorrente contaDestino = contaA.getId().equals(idContaDestino) ? contaA : contaB;

        if (!contaOrigem.possuiSaldoSuficiente(valor)) {
            throw new SaldoInsuficienteException("Saldo insuficiente para PIX.");
        }

        contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(valor));
        contaDestino.setSaldo(contaDestino.getSaldo().add(valor));

        contaCorrenteRepository.save(contaOrigem);
        contaCorrenteRepository.save(contaDestino);

        registrarTransacao(origem, valor, TipoTransacao.PIX, "PIX enviado para " + destino.getNome());
        registrarTransacao(destino, valor, TipoTransacao.PIX, "PIX recebido de " + origem.getNome());

        return "PIX realizado com sucesso.";
    }

    @Transactional
    public String transferir(Long usuarioOrigemId, Long usuarioDestinoId, BigDecimal valor) {
        validarValor(valor);

        if (usuarioOrigemId.equals(usuarioDestinoId)) {
            throw new NegocioException("Não é possível fazer transferência para a própria conta.");
        }

        Usuario origem = buscarUsuario(usuarioOrigemId);
        Usuario destino = buscarUsuario(usuarioDestinoId);

        Long idContaOrigem = origem.getConta().getId();
        Long idContaDestino = destino.getConta().getId();

        // Lock em ordem crescente de ID para evitar deadlock
        ContaCorrente contaA = bloquearConta(Math.min(idContaOrigem, idContaDestino));
        ContaCorrente contaB = bloquearConta(Math.max(idContaOrigem, idContaDestino));

        ContaCorrente contaOrigem = contaA.getId().equals(idContaOrigem) ? contaA : contaB;
        ContaCorrente contaDestino = contaA.getId().equals(idContaDestino) ? contaA : contaB;

        if (!contaOrigem.possuiSaldoSuficiente(valor)) {
            throw new SaldoInsuficienteException("Saldo insuficiente para transferência.");
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

    private ContaCorrente bloquearConta(Long contaId) {
        return contaCorrenteRepository.findByIdWithLock(contaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada."));
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    private void validarValor(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("O valor deve ser maior que zero.");
        }
    }
}
