package acc.br.nextpay;

import acc.br.nextpay.controller.TratadorGlobalDeExcecoes;
import acc.br.nextpay.exception.NegocioException;
import acc.br.nextpay.exception.RecursoNaoEncontradoException;
import acc.br.nextpay.exception.SaldoInsuficienteException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class TratadorGlobalDeExcecoesTest {

    private final TratadorGlobalDeExcecoes tratador = new TratadorGlobalDeExcecoes();

    @Test
    void tratarNaoEncontrado_deveRetornar404() {
        RecursoNaoEncontradoException ex = new RecursoNaoEncontradoException("Recurso não encontrado");

        ResponseEntity<Map<String, String>> resposta = tratador.tratarNaoEncontrado(ex);

        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals("Recurso não encontrado", resposta.getBody().get("erro"));
    }

    @Test
    void tratarSaldoInsuficiente_deveRetornar422() {
        SaldoInsuficienteException ex = new SaldoInsuficienteException("Saldo insuficiente");

        ResponseEntity<Map<String, String>> resposta = tratador.tratarSaldoInsuficiente(ex);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals("Saldo insuficiente", resposta.getBody().get("erro"));
    }

    @Test
    void tratarNegocio_deveRetornar400() {
        NegocioException ex = new NegocioException("Regra de negócio violada");

        ResponseEntity<Map<String, String>> resposta = tratador.tratarNegocio(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals("Regra de negócio violada", resposta.getBody().get("erro"));
    }

    @Test
    void tratarErroGenerico_deveRetornar500() {
        RuntimeException ex = new RuntimeException("Erro inesperado");

        ResponseEntity<Map<String, String>> resposta = tratador.tratarErroGenerico(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals("Erro interno. Tente novamente.", resposta.getBody().get("erro"));
    }
}
