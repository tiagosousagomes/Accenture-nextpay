package acc.br.nextpay;

import acc.br.nextpay.model.*;
import acc.br.nextpay.model.enums.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ModelTotalTest {

    @Test
    void testCoberturaTotalModels() {
        Usuario u1 = new Usuario();
        u1.setId(1L);
        u1.setNome("Teste");
        u1.setEmail("teste@teste.com");
        u1.setSenha("123");
        u1.setCpfCnpj("123456789");
        u1.setNivel(NivelUsuario.BRONZE);
        u1.setPontuacao(100);
        u1.setFotoPerfilUrl("http://foto.com");

        ContaCorrente cc = ContaCorrente.builder()
                .id(1L)
                .saldo(BigDecimal.TEN)
                .limite(BigDecimal.ONE)
                .build();
        u1.setConta(cc);

        Produto p = new Produto();
        p.setId(1L);
        p.setNome("Produto Teste");
        p.setPreco(BigDecimal.valueOf(50.0));
        p.setQuantidadeEstoque(10);
        p.setVendedor(u1);

        Pedido ped = new Pedido();
        ped.setId(1L);
        ped.setComprador(u1);
        ped.setVendedor(u1);
        ped.setProduto(p);
        ped.setQuantidade(2);
        ped.setValorTotal(BigDecimal.valueOf(100.0));
        ped.setStatus(StatusPedido.FINALIZADO);
        ped.setDataPedido(LocalDateTime.now());

        Transacao t = Transacao.builder()
                .id(1L)
                .usuario(u1)
                .valor(BigDecimal.TEN)
                .tipo(TipoTransacao.DEPOSITO)
                .data(LocalDateTime.now())
                .descricao("Teste")
                .build();

        assertNotNull(u1.toString());
        assertNotNull(cc.toString());
        assertNotNull(p.toString());
        assertNotNull(ped.toString());
        assertNotNull(t.toString());

        assertNotNull(u1.hashCode());
        assertEquals(u1, u1);
        assertNotEquals(u1, new Object());

        assertEquals("Teste", u1.getNome());
        assertEquals(BigDecimal.TEN, cc.getSaldo());
        assertEquals(StatusPedido.FINALIZADO, ped.getStatus());
    }
}