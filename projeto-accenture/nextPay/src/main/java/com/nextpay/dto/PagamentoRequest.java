package com.nextpay.dto;

import com.nextpay.entity.Pagamento;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PagamentoRequest(
        @NotNull UUID contaOrigemId,
        @NotNull Pagamento.MetodoPagamento metodo
) {}
