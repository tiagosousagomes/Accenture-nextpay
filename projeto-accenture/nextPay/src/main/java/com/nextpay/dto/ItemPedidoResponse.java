package com.nextpay.dto;

import com.nextpay.entity.ItemPedido;
import java.math.BigDecimal;
import java.util.UUID;

public record ItemPedidoResponse(
        UUID id,
        UUID produtoId,
        String produtoNome,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {
    public static ItemPedidoResponse from(ItemPedido i) {
        var sub = i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade()));
        return new ItemPedidoResponse(
                i.getId(), i.getProduto().getId(), i.getProduto().getNome(),
                i.getQuantidade(), i.getPrecoUnitario(), sub
        );
    }
}
