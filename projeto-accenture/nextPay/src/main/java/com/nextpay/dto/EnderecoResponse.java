package com.nextpay.dto;

import com.nextpay.entity.Endereco;
import java.util.UUID;

public record EnderecoResponse(
        UUID id,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String cidade,
        String uf,
        Endereco.TipoEndereco tipo
) {
    public static EnderecoResponse from(Endereco e) {
        return new EnderecoResponse(
                e.getId(), e.getCep(), e.getLogradouro(), e.getNumero(),
                e.getComplemento(), e.getCidade(), e.getUf(), e.getTipo()
        );
    }
}
