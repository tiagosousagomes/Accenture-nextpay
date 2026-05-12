package com.nextpay.dto;

import com.nextpay.entity.ResgateCashback;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ResgateCashbackResponse(
        UUID id,
        UUID clienteId,
        ResgateCashback.TipoResgate tipo,
        Long pontosConvertidos,
        BigDecimal valorReal,
        ResgateCashback.StatusResgate status,
        LocalDateTime criadoEm
) {
    public static ResgateCashbackResponse from(ResgateCashback r) {
        return new ResgateCashbackResponse(
                r.getId(), r.getCliente().getId(), r.getTipo(),
                r.getPontosConvertidos(), r.getValorReal(), r.getStatus(), r.getCriadoEm()
        );
    }
}
