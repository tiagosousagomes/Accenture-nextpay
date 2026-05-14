package acc.br.nextpay;

import acc.br.nextpay.model.Transacao;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.model.enums.TipoTransacao;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class TransacaoModelTest {

    @Test
    void testTransacaoGettersSettersAndBuilder() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        LocalDateTime agora = LocalDateTime.now();

        Transacao t = Transacao.builder()
                .id(1L)
                .usuario(usuario)
                .tipo(TipoTransacao.DEPOSITO)
                .valor(BigDecimal.TEN)
                .data(agora)
                .descricao("Teste")
                .build();

        assertEquals(1L, t.getId());
        assertEquals(usuario, t.getUsuario());
        assertEquals(TipoTransacao.DEPOSITO, t.getTipo());
        assertEquals(BigDecimal.TEN, t.getValor());
        assertEquals(agora, t.getData());
        assertEquals("Teste", t.getDescricao());

        Transacao t2 = new Transacao();
        t2.setDescricao("Setado");
        assertEquals("Setado", t2.getDescricao());
    }

    @Test
    void testOnCreate() {
        Transacao t = new Transacao();

        t.onCreate();

        assertNotNull(t.getData());
    }

    @Test
    void testToStringAndEquals() {
        Transacao t1 = Transacao.builder().id(1L).build();
        Transacao t2 = Transacao.builder().id(1L).build();

        assertNotNull(t1.toString());
        assertEquals(t1.getId(), t2.getId());
        assertNotEquals(t1, new Object());
    }
}