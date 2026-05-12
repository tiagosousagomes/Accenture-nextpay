package com.nextpay.dto;

import com.nextpay.entity.Transacao;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record TransacaoRequest(
        @NotNull UUID contaId,
        @NotNull Transacao.TipoTransacao tipo,
        @NotNull Transacao.CategoriaTransacao categoria,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
        String descricao,
        UUID contaDestinoId
) {}
