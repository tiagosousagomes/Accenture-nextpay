package acc.br.nextpay;

import acc.br.nextpay.model.Endereco;
import acc.br.nextpay.service.ViaCepService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ViaCepServiceTest {

    private final ViaCepService viaCepService = new ViaCepService();

    @Test
    void testBuscarEnderecoComSucesso() {

        String cep = "01001000";
        Endereco resultado = viaCepService.buscarEnderecoPorCep(cep);

        if (resultado != null) {
            assertNotNull(resultado.getLogradouro());

            assertFalse(resultado.getCep().contains("-"));
        }
    }

    @Test
    void testBuscarEnderecoCepInvalido() {

        Endereco resultado = viaCepService.buscarEnderecoPorCep("999");
        assertNull(resultado);
    }
}