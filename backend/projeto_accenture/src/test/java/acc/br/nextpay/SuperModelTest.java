package acc.br.nextpay;

import acc.br.nextpay.model.*;
import acc.br.nextpay.model.enums.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SuperModelTest {
    @Test
    void testCompleto() {
        Usuario u = new Usuario();
        u.setId(1L); u.setNome("A"); u.setEmail("a@a.com"); u.setNivel(NivelUsuario.BRONZE);

        Produto p = new Produto();
        p.setId(1L); p.setNome("P"); p.setPreco(BigDecimal.TEN);

        Pedido ped = new Pedido();
        ped.setId(1L); ped.setStatus(StatusPedido.FINALIZADO);

        assertNotNull(u.toString());
        assertNotNull(p.hashCode());
        assertEquals("A", u.getNome());
    }
}