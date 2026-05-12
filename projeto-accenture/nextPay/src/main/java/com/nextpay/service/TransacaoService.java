package com.nextpay.service;

import com.nextpay.dto.TransacaoRequest;
import com.nextpay.entity.*;
import com.nextpay.exception.BusinessException;
import com.nextpay.exception.ResourceNotFoundException;
import com.nextpay.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TransacaoService {

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;
    private final CashbackService cashbackService;

    public TransacaoService(ContaRepository contaRepository,
                            TransacaoRepository transacaoRepository,
                            CashbackService cashbackService) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
        this.cashbackService = cashbackService;
    }

    /**
     * Endpoint público de transações. Regras por tipo:
     *  - DEPOSITO: entrada na própria conta. Ignora contaDestinoId.
     *  - PIX: transferência entre contas distintas. Exige contaDestinoId.
     *         Gera duas transações: saída na origem + entrada no destino.
     *  - COMPRA: proibido. Use POST /api/pedidos/{id}/pagar.
     *  - ESTORNO / CREDITO_CASHBACK: uso interno, proibidos por aqui.
     */
    @Transactional
    public Transacao registrar(TransacaoRequest req) {
        return switch (req.tipo()) {
            case DEPOSITO -> depositar(req);
            case PIX -> transferirPix(req);
            case COMPRA -> throw new BusinessException(
                    "Compras devem ser feitas pagando um Pedido em POST /api/pedidos/{id}/pagar");
            case ESTORNO, CREDITO_CASHBACK -> throw new BusinessException(
                    "Tipo " + req.tipo() + " é gerado internamente e não pode ser criado via API");
        };
    }

    private Transacao depositar(TransacaoRequest req) {
        if (req.contaDestinoId() != null) {
            throw new BusinessException("Depósito não usa contaDestinoId");
        }
        var conta = contaAtiva(req.contaId());
        conta.setSaldo(conta.getSaldo().add(req.valor()));
        contaRepository.save(conta);

        var t = Transacao.builder()
                .conta(conta)
                .tipo(Transacao.TipoTransacao.DEPOSITO)
                .categoria(req.categoria())
                .valor(req.valor())
                .status(Transacao.StatusTransacao.CONCLUIDA)
                .descricao(req.descricao())
                .valorCashbackGerado(BigDecimal.ZERO)
                .build();
        t = transacaoRepository.save(t);
        cashbackService.calcularEAplicarCashback(t);
        return t;
    }

    private Transacao transferirPix(TransacaoRequest req) {
        if (req.contaDestinoId() == null) {
            throw new BusinessException("PIX exige contaDestinoId");
        }
        if (req.contaDestinoId().equals(req.contaId())) {
            throw new BusinessException("Conta origem e destino não podem ser iguais");
        }
        var origem = contaAtiva(req.contaId());
        var destino = contaAtiva(req.contaDestinoId());

        BigDecimal disponivel = origem.getSaldo().add(origem.getLimite());
        if (disponivel.compareTo(req.valor()) < 0) {
            throw new BusinessException("Saldo insuficiente");
        }

        origem.setSaldo(origem.getSaldo().subtract(req.valor()));
        destino.setSaldo(destino.getSaldo().add(req.valor()));
        contaRepository.save(origem);
        contaRepository.save(destino);

        var saida = transacaoRepository.save(Transacao.builder()
                .conta(origem)
                .tipo(Transacao.TipoTransacao.PIX)
                .categoria(req.categoria())
                .valor(req.valor())
                .status(Transacao.StatusTransacao.CONCLUIDA)
                .descricao(prefixar("PIX enviado", req.descricao()))
                .valorCashbackGerado(BigDecimal.ZERO)
                .build());

        transacaoRepository.save(Transacao.builder()
                .conta(destino)
                .tipo(Transacao.TipoTransacao.PIX)
                .categoria(req.categoria())
                .valor(req.valor())
                .status(Transacao.StatusTransacao.CONCLUIDA)
                .descricao(prefixar("PIX recebido", req.descricao()))
                .valorCashbackGerado(BigDecimal.ZERO)
                .build());

        cashbackService.calcularEAplicarCashback(saida);
        return saida;
    }

    private Conta contaAtiva(UUID contaId) {
        var conta = contaRepository.findById(contaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + contaId));
        if (conta.getStatus() != Conta.StatusConta.ATIVA) {
            throw new BusinessException("Conta não está ativa: " + contaId);
        }
        return conta;
    }

    private String prefixar(String prefixo, String descricao) {
        if (descricao == null || descricao.isBlank()) return prefixo;
        return prefixo + " - " + descricao;
    }

    @Transactional(readOnly = true)
    public List<Transacao> extratoPorCliente(UUID clienteId) {
        return transacaoRepository.findByContaClienteIdOrderByDataDesc(clienteId);
    }

    @Transactional(readOnly = true)
    public List<Transacao> extratoPorConta(UUID contaId) {
        return transacaoRepository.findByContaIdOrderByDataDesc(contaId);
    }
}
