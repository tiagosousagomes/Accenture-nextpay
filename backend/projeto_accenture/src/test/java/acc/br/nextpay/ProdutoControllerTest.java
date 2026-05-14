package acc.br.nextpay;

import acc.br.nextpay.controller.ProdutoController;
import acc.br.nextpay.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProdutoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProdutoService produtoService;

    @Test
    void testListarTodos() throws Exception {
        Mockito.when(produtoService.listarProdutosComEstoque()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk());
    }

    @Test
    void testCadastrar() throws Exception {
        mockMvc.perform(post("/api/produtos/usuario/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Teste\", \"preco\":10.0}"))
                .andExpect(status().isCreated());
    }
}