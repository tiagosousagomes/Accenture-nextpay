package com.nextpay.dto;

import com.nextpay.entity.Conta;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ContaResponse(
        UUID id,
        UUID clienteId,
        String agencia,
        String numero,
        String digito,
        BigDecimal saldo,
        BigDecimal limite,
        Conta.StatusConta status,
        LocalDateTime criadoEm
) {
    public static ContaResponse from(Conta c) {
        return new ContaResponse(
                c.getId(), c.getCliente().getId(), c.getAgencia(), c.getNumero(),
                c.getDigito(), c.getSaldo(), c.getLimite(), c.getStatus(), c.getCriadoEm()
        );
    }
}
