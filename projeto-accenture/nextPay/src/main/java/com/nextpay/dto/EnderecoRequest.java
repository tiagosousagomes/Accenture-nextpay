package com.nextpay.dto;

import com.nextpay.entity.Endereco;
import jakarta.validation.constraints.*;

public record EnderecoRequest(
        @NotBlank @Pattern(regexp = "\\d{5}-?\\d{3}") String cep,
        String logradouro,
        @NotBlank String numero,
        String complemento,
        String cidade,
        @Size(min = 2, max = 2) String uf,
        @NotNull Endereco.TipoEndereco tipo
) {}
