package com.nextpay.dto;

import com.nextpay.entity.Transacao;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransacaoResponse(
        UUID id,
        UUID contaId,
        UUID pagamentoId,
        Transacao.TipoTransacao tipo,
        Transacao.CategoriaTransacao categoria,
        BigDecimal valor,
        Transacao.StatusTransacao status,
        BigDecimal valorCashbackGerado,
        String descricao,
        LocalDateTime data
) {
    public static TransacaoResponse from(Transacao t) {
        return new TransacaoResponse(
                t.getId(), t.getConta().getId(),
                t.getPagamento() != null ? t.getPagamento().getId() : null,
                t.getTipo(), t.getCategoria(), t.getValor(), t.getStatus(),
                t.getValorCashbackGerado(), t.getDescricao(), t.getData()
        );
    }
}
