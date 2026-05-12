package com.nextpay.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public record PedidoRequest(
        @NotNull UUID compradorId,
        @NotNull UUID enderecoEntregaId,
        @NotEmpty @Valid List<ItemPedidoRequest> itens
) {}
