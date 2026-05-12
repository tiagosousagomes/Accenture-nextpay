package com.nextpay.dto;

import com.nextpay.entity.Pagamento;
import java.time.LocalDateTime;
import java.util.UUID;

public record PagamentoResponse(
        UUID id,
        UUID pedidoId,
        UUID contaOrigemId,
        Pagamento.MetodoPagamento metodo,
        Pagamento.StatusPagamento status,
        LocalDateTime pagoEm,
        UUID transacaoId
) {
    public static PagamentoResponse from(Pagamento p) {
        UUID transacaoId = p.getTransacoes().isEmpty() ? null : p.getTransacoes().get(0).getId();
        return new PagamentoResponse(
                p.getId(),
                p.getPedido().getId(),
                p.getContaOrigem().getId(),
                p.getMetodo(),
                p.getStatus(),
                p.getPagoEm(),
                transacaoId
        );
    }
}
