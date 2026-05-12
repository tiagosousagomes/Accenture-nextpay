package com.nextpay.service;

import com.nextpay.entity.*;
import com.nextpay.exception.BusinessException;
import com.nextpay.exception.ResourceNotFoundException;
import com.nextpay.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CashbackService {

    private static final BigDecimal CEM = new BigDecimal("100");
    private static final BigDecimal PONTOS_POR_REAL = new BigDecimal("100"); // 100 pontos = R$1
    private static final int DIAS_LIBERACAO = 30;

    private final ClienteRepository clienteRepository;
    private final TransacaoRepository transacaoRepository;
    private final RegraCashbackRepository regraRepository;
    private final HistoricoNivelRepository historicoRepository;
    private final ResgateCashbackRepository resgateRepository;

    public CashbackService(ClienteRepository clienteRepository,
                           TransacaoRepository transacaoRepository,
                           RegraCashbackRepository regraRepository,
                           HistoricoNivelRepository historicoRepository,
                           ResgateCashbackRepository resgateRepository) {
        this.clienteRepository = clienteRepository;
        this.transacaoRepository = transacaoRepository;
        this.regraRepository = regraRepository;
        this.historicoRepository = historicoRepository;
        this.resgateRepository = resgateRepository;
    }

    /**
     * Calcula e aplica cashback para uma transação CONCLUÍDA.
     */
    @Transactional
    public void calcularEAplicarCashback(Transacao transacao) {
        if (transacao.getStatus() != Transacao.StatusTransacao.CONCLUIDA) {
            return;
        }
        if (transacao.getTipo() == Transacao.TipoTransacao.ESTORNO
                || transacao.getTipo() == Transacao.TipoTransacao.CREDITO_CASHBACK) {
            return;
        }

        var cliente = transacao.getConta().getCliente();
        var regra = buscarMelhorRegra(transacao.getCategoria(), cliente.getNivel());

        BigDecimal percentual = regra.map(RegraCashback::getPercentual).orElse(BigDecimal.ZERO);
        BigDecimal cashback = transacao.getValor()
                .multiply(percentual)
                .divide(CEM, 2, RoundingMode.HALF_UP);

        transacao.setValorCashbackGerado(cashback);
        transacaoRepository.save(transacao);

        if (cashback.compareTo(BigDecimal.ZERO) <= 0) return;

        long pontosGerados = cashback.multiply(PONTOS_POR_REAL).longValue();
        cliente.setPontosAcumulados(cliente.getPontosAcumulados() + pontosGerados);

        if (liberacaoInstantanea(cliente.getNivel())) {
            cliente.setSaldoCashback(cliente.getSaldoCashback().add(cashback));
        }

        atualizarNivelSeNecessario(cliente);
        clienteRepository.save(cliente);
    }

    /**
     * Libera cashbacks pendentes que já completaram 30 dias.
     */
    @Transactional
    public int liberarCashbacksPendentes() {
        var corte = LocalDateTime.now().minusDays(DIAS_LIBERACAO);
        var pendentes = transacaoRepository.findByStatusAndDataBefore(
                Transacao.StatusTransacao.CONCLUIDA, corte);

        int liberadas = 0;
        for (Transacao t : pendentes) {
            if (t.getValorCashbackGerado() == null
                    || t.getValorCashbackGerado().compareTo(BigDecimal.ZERO) <= 0) continue;

            var cliente = t.getConta().getCliente();
            if (liberacaoInstantanea(cliente.getNivel())) continue;

            cliente.setSaldoCashback(cliente.getSaldoCashback().add(t.getValorCashbackGerado()));
            clienteRepository.save(cliente);
            liberadas++;
        }
        return liberadas;
    }

    @Transactional
    public ResgateCashback resgatar(UUID clienteId, Long pontos) {
        if (pontos == null || pontos < 100) {
            throw new BusinessException("Resgate mínimo: 100 pontos");
        }

        var cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: " + clienteId));

        if (cliente.getPontosAcumulados() < pontos) {
            throw new BusinessException("Pontos insuficientes. Disponível: " + cliente.getPontosAcumulados());
        }

        BigDecimal valorReal = new BigDecimal(pontos)
                .divide(PONTOS_POR_REAL, 2, RoundingMode.HALF_UP);

        cliente.setPontosAcumulados(cliente.getPontosAcumulados() - pontos);
        cliente.setSaldoCashback(cliente.getSaldoCashback().add(valorReal));

        var resgate = ResgateCashback.builder()
                .cliente(cliente)
                .tipo(ResgateCashback.TipoResgate.CREDITO_CONTA)
                .pontosConvertidos(pontos)
                .valorReal(valorReal)
                .status(ResgateCashback.StatusResgate.CONCLUIDO)
                .build();

        resgateRepository.save(resgate);
        clienteRepository.save(cliente);
        return resgate;
    }

    @Transactional
    public void atualizarNivelSeNecessario(Cliente cliente) {
        var novoNivel = calcularNivel(cliente.getPontosAcumulados());
        if (novoNivel == cliente.getNivel()) return;

        var anterior = cliente.getNivel();
        cliente.setNivel(novoNivel);

        var hist = HistoricoNivel.builder()
                .cliente(cliente)
                .nivelAnterior(anterior)
                .nivelAtual(novoNivel)
                .pontosNoMomento(cliente.getPontosAcumulados())
                .motivo("Atualização automática por acúmulo de pontos")
                .build();
        historicoRepository.save(hist);
    }

    @Transactional(readOnly = true)
    public BigDecimal saldoCashback(UUID clienteId) {
        return clienteRepository.findById(clienteId)
                .map(Cliente::getSaldoCashback)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: " + clienteId));
    }

    public Cliente.NivelCliente calcularNivel(long pontos) {
        if (pontos >= 7000) return Cliente.NivelCliente.PLATINA;
        if (pontos >= 3000) return Cliente.NivelCliente.OURO;
        if (pontos >= 1000) return Cliente.NivelCliente.PRATA;
        return Cliente.NivelCliente.BRONZE;
    }

    public long pontosParaProximoNivel(long pontosAtuais) {
        if (pontosAtuais < 1000) return 1000 - pontosAtuais;
        if (pontosAtuais < 3000) return 3000 - pontosAtuais;
        if (pontosAtuais < 7000) return 7000 - pontosAtuais;
        return 0;
    }

    public Cliente.NivelCliente proximoNivel(Cliente.NivelCliente atual) {
        return switch (atual) {
            case BRONZE -> Cliente.NivelCliente.PRATA;
            case PRATA -> Cliente.NivelCliente.OURO;
            case OURO -> Cliente.NivelCliente.PLATINA;
            case PLATINA -> Cliente.NivelCliente.PLATINA;
        };
    }

    private boolean liberacaoInstantanea(Cliente.NivelCliente nivel) {
        return nivel == Cliente.NivelCliente.OURO || nivel == Cliente.NivelCliente.PLATINA;
    }

    private java.util.Optional<RegraCashback> buscarMelhorRegra(
            Transacao.CategoriaTransacao categoria, Cliente.NivelCliente nivelCliente) {
        var niveisElegiveis = niveisAteh(nivelCliente);
        return regraRepository.findMelhorRegra(categoria, niveisElegiveis);
    }

    private List<Cliente.NivelCliente> niveisAteh(Cliente.NivelCliente nivel) {
        return switch (nivel) {
            case BRONZE -> List.of(Cliente.NivelCliente.BRONZE);
            case PRATA -> List.of(Cliente.NivelCliente.BRONZE, Cliente.NivelCliente.PRATA);
            case OURO -> List.of(Cliente.NivelCliente.BRONZE, Cliente.NivelCliente.PRATA,
                    Cliente.NivelCliente.OURO);
            case PLATINA -> List.of(Cliente.NivelCliente.values());
        };
    }
}
