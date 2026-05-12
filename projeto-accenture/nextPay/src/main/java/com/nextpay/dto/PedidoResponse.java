package com.nextpay.dto;

import com.nextpay.entity.Pedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PedidoResponse(
        UUID id,
        UUID compradorId,
        UUID enderecoEntregaId,
        BigDecimal valorTotal,
        Pedido.StatusPedido status,
        BigDecimal cashbackGerado,
        LocalDateTime data,
        List<ItemPedidoResponse> itens
) {
    public static PedidoResponse from(Pedido p) {
        var itens = p.getItens().stream().map(ItemPedidoResponse::from).toList();
        return new PedidoResponse(
                p.getId(), p.getComprador().getId(), p.getEnderecoEntrega().getId(),
                p.getValorTotal(), p.getStatus(), p.getCashbackGerado(), p.getData(), itens
        );
    }
}
