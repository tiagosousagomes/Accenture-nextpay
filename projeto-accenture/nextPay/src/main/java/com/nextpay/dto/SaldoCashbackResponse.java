package com.nextpay.dto;

import com.nextpay.entity.Cliente;
import java.math.BigDecimal;
import java.util.UUID;

public record SaldoCashbackResponse(
        UUID clienteId,
        Cliente.NivelCliente nivel,
        Long pontosAcumulados,
        BigDecimal saldoCashback,
        Long pontosParaProximoNivel,
        Cliente.NivelCliente proximoNivel
) {}
