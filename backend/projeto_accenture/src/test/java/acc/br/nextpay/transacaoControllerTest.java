package acc.br.nextpay;

import acc.br.nextpay.controller.TransacaoController;
import acc.br.nextpay.service.TransacaoService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransacaoController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransacaoService transacaoService;

    @Test
    void testGetHistorico() throws Exception {

        Mockito.when(transacaoService.listarTransacoesPorUsuario(1L))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/transacoes/usuario/1"))
                .andExpect(status().isOk());
    }
}