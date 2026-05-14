package acc.br.nextpay;

import acc.br.nextpay.controller.ProdutoController;
import acc.br.nextpay.model.Produto;
import acc.br.nextpay.security.JwtUtil;
import acc.br.nextpay.service.ProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @MockBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        var auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testListarTodos() throws Exception {
        Mockito.when(produtoService.listarProdutosComEstoque()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk());
    }

    @Test
    void testCadastrar() throws Exception {
        Produto produto = new Produto();
        produto.setId(1L);
        produto.setNome("Teclado");

        Mockito.when(produtoService.cadastrarProduto(Mockito.any(Produto.class), Mockito.anyLong()))
                .thenReturn(produto);

        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Teclado\", \"preco\":10.0, \"quantidadeEstoque\":5}"))
                .andExpect(status().isCreated());
    }

    @Test
    void testListarMeusProdutos() throws Exception {
        Mockito.when(produtoService.listarProdutosDoUsuario(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/produtos/meus"))
                .andExpect(status().isOk());
    }

    @Test
    void testListarProdutosDoUsuario() throws Exception {
        Mockito.when(produtoService.listarProdutosDoUsuario(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/produtos/usuario/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testEditar() throws Exception {
        Produto produto = new Produto();
        produto.setId(1L);
        produto.setNome("Novo Nome");

        Mockito.when(produtoService.editarProduto(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(Produto.class)))
                .thenReturn(produto);

        mockMvc.perform(put("/api/produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Novo Nome\", \"preco\":20.0, \"quantidadeEstoque\":3}"))
                .andExpect(status().isOk());
    }

    @Test
    void testExcluir() throws Exception {
        Mockito.doNothing().when(produtoService).excluirProduto(Mockito.anyLong(), Mockito.anyLong());

        mockMvc.perform(delete("/api/produtos/1"))
                .andExpect(status().isOk());
    }
}
