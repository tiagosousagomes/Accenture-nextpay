package com.nextpay.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record ResgateCashbackRequest(
        @NotNull UUID clienteId,
        @NotNull @Min(100) Long pontos
) {}
