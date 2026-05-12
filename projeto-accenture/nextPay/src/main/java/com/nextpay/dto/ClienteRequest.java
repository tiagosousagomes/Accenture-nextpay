package com.nextpay.dto;

import com.nextpay.entity.Cliente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record ClienteRequest(
        @NotBlank @Size(min = 3, max = 120) String nome,
        @NotBlank @Pattern(regexp = "\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}",
                message = "CPF inválido") String cpf,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 100) String senha,
        @NotNull Cliente.TipoCliente tipo,
        @Valid List<EnderecoRequest> enderecos
) {}
