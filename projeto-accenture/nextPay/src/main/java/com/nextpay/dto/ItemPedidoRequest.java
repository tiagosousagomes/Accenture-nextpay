package com.nextpay.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record ItemPedidoRequest(
        @NotNull UUID produtoId,
        @NotNull @Min(1) Integer quantidade
) {}
