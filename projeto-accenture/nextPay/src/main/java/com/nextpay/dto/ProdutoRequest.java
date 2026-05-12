package com.nextpay.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoRequest(
        @NotNull UUID vendedorId,
        @NotBlank String nome,
        @NotNull @DecimalMin(value = "0.01") BigDecimal preco,
        @NotNull @Min(0) Integer estoque,
        String imagemUrl
) {}
