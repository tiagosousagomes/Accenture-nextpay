package acc.br.nextpay;

import acc.br.nextpay.ai.AssistenteRegrasNegocio;
import acc.br.nextpay.controller.AjudaController;
import acc.br.nextpay.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AjudaController.class)
@AutoConfigureMockMvc(addFilters = false)
class AjudaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssistenteRegrasNegocio assistenteRegrasNegocio;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void testPerguntar() throws Exception {
        Mockito.when(assistenteRegrasNegocio.perguntar(Mockito.anyString()))
                .thenReturn("Resposta simulada");

        mockMvc.perform(post("/api/ajuda/perguntar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pergunta\":\"Como investir?\"}"))
                .andExpect(status().isOk());
    }
}