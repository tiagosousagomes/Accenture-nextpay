package acc.br.nextpay;

import acc.br.nextpay.dto.ConfirmacaoEmailDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfirmacaoEmailDTOTest {

    @Test
    void deveTestarGettersESettersComSucesso() {
        // Cenário: Instanciação do DTO
        ConfirmacaoEmailDTO dto = new ConfirmacaoEmailDTO();

        String emailEsperado = "suporte@nextpay.com";
        String codigoEsperado = "987654";

        // Ação: Injeção de dados usando os Setters
        dto.setEmail(emailEsperado);
        dto.setCodigo(codigoEsperado);

        // Validação: Verificação se os Getters retornam exatamente o que foi injetado
        Assertions.assertNotNull(dto);
        Assertions.assertEquals(emailEsperado, dto.getEmail());
        Assertions.assertEquals(codigoEsperado, dto.getCodigo());
    }
}