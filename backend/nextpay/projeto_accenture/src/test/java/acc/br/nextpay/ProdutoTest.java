package acc.br.nextpay;

import acc.br.nextpay.model.Produto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

// CORREÇÃO: Adicionando o import faltante para o compilador achar a classe Usuario
import acc.br.nextpay.model.Usuario;

class ProdutoTest {

    @Test
    void deveValidarControleDeEstoqueDisponivel() {
        Produto produto = new Produto();
        produto.setQuantidadeEstoque(10);

        // Cenário 1: Quantidade solicitada menor ou igual ao estoque
        Assertions.assertTrue(produto.temEstoqueDisponivel(5));
        Assertions.assertTrue(produto.temEstoqueDisponivel(10));

        // Cenário 2: Quantidade solicitada maior que o estoque disponível
        Assertions.assertFalse(produto.temEstoqueDisponivel(11));

        // Cenário 3: Quantidade solicitada inválida (zero ou negativa)
        Assertions.assertFalse(produto.temEstoqueDisponivel(0));
        Assertions.assertFalse(produto.temEstoqueDisponivel(-2));
    }

    @Test
    void deveBaixarEstoqueComSucesso() {
        Produto produto = new Produto();
        produto.setQuantidadeEstoque(10);

        produto.baixarEstoque(4);

        Assertions.assertEquals(6, produto.getQuantidadeEstoque());
    }

    @Test
    void deveLancarExcecaoAoBaixarEstoqueInsuficiente() {
        Produto produto = new Produto();
        produto.setQuantidadeEstoque(3);

        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
            produto.baixarEstoque(5);
        });

        Assertions.assertEquals("Estoque insuficiente para este produto.", exception.getMessage());
    }

    @Test
    void deveDevolverEstoqueComSucesso() {
        Produto produto = new Produto();
        produto.setQuantidadeEstoque(5);

        produto.devolverEstoque(3);

        Assertions.assertEquals(8, produto.getQuantidadeEstoque());
    }

    @Test
    void deveCobrirGettersSettersEBuildersDoLombok() {
        // Agora o compilador vai reconhecer o Usuario perfeitamente
        Usuario vendedor = new Usuario();
        BigDecimal preco = new BigDecimal("150.00");

        Produto produtoCompleto = new Produto(
                "Eletrônicos", 1L, "Mouse", "Mouse sem fio",
                preco, 20, vendedor, "base64_string_foto"
        );

        Assertions.assertEquals("Eletrônicos", produtoCompleto.getCategoria());
        Assertions.assertEquals(1L, produtoCompleto.getId());
        Assertions.assertEquals("Mouse", produtoCompleto.getNome());
        Assertions.assertEquals("Mouse sem fio", produtoCompleto.getDescricao());
        Assertions.assertEquals(preco, produtoCompleto.getPreco());
        Assertions.assertEquals(20, produtoCompleto.getQuantidadeEstoque());
        Assertions.assertEquals(vendedor, produtoCompleto.getVendedor());
        Assertions.assertEquals("base64_string_foto", produtoCompleto.getFotoProduto());

        // Testa o Builder do Lombok
        Produto produtoBuilder = Produto.builder()
                .categoria("Móveis")
                .id(2L)
                .nome("Cadeira")
                .descricao("Cadeira Gamer")
                .preco(new BigDecimal("800.00"))
                .quantidadeEstoque(15)
                .vendedor(vendedor)
                .fotoProduto("foto_cadeira")
                .build();

        Assertions.assertNotNull(produtoBuilder);
        Assertions.assertEquals(2L, produtoBuilder.getId());
        Assertions.assertEquals("Cadeira", produtoBuilder.getNome());
    }
}