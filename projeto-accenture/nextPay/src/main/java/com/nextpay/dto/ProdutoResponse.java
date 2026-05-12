package com.nextpay.dto;

import com.nextpay.entity.Produto;
import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoResponse(
        UUID id,
        UUID vendedorId,
        String nome,
        BigDecimal preco,
        Integer estoque,
        String imagemUrl,
        Boolean ativo
) {
    public static ProdutoResponse from(Produto p) {
        return new ProdutoResponse(
                p.getId(), p.getVendedor().getId(), p.getNome(), p.getPreco(),
                p.getEstoque(), p.getImagemUrl(), p.getAtivo()
        );
    }
}
