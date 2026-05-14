package acc.br.nextpay;

import acc.br.nextpay.controller.PedidoController;
import acc.br.nextpay.service.PedidoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    @Test
    void testComprar() throws Exception {
        mockMvc.perform(post("/api/pedidos/comprar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"compradorId\":1, \"produtoId\":1, \"quantidade\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    void testListarTodos() throws Exception {
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk());
    }
}