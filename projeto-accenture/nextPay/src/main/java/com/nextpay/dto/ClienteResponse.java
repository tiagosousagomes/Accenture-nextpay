package com.nextpay.dto;

import com.nextpay.entity.Cliente;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ClienteResponse(
        UUID id,
        String nome,
        String cpf,
        String email,
        Cliente.TipoCliente tipo,
        Cliente.NivelCliente nivel,
        Long pontosAcumulados,
        BigDecimal saldoCashback,
        LocalDateTime criadoEm,
        List<ContaResponse> contas,
        List<EnderecoResponse> enderecos
) {
    public static ClienteResponse from(Cliente c) {
        var contas = c.getContas() == null ? List.<ContaResponse>of() :
                c.getContas().stream().map(ContaResponse::from).toList();
        var enderecos = c.getEnderecos() == null ? List.<EnderecoResponse>of() :
                c.getEnderecos().stream().map(EnderecoResponse::from).toList();
        return new ClienteResponse(
                c.getId(), c.getNome(), c.getCpf(), c.getEmail(), c.getTipo(),
                c.getNivel(), c.getPontosAcumulados(), c.getSaldoCashback(), c.getCriadoEm(),
                contas, enderecos
        );
    }
}
