package acc.br.nextpay;

import acc.br.nextpay.controller.UsuarioController;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    void testListarTodos() throws Exception {
        Mockito.when(usuarioService.listarTodos()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    void testBuscarPorId() throws Exception {
        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testCadastrar() throws Exception {
        Usuario u = new Usuario();
        u.setNome("Jose Silva");
        u.setEmail("jose@email.com");
        u.setSenha("Senha123!");
        u.setCpfCnpj("12345678901");

        Mockito.when(usuarioService.cadastrarUsuario(any(Usuario.class), anyString(), anyString()))
                .thenReturn(u);

        mockMvc.perform(post("/api/usuarios")
                        .param("cep", "01001000")
                        .param("numero", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(u)))
                .andExpect(status().isCreated());
    }

    @Test
    void testAtualizar() throws Exception {
        Usuario u = new Usuario();
        u.setNome("Novo Nome");

        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(u)))
                .andExpect(status().isOk());
    }

    @Test
    void testExcluirConta() throws Exception {
        mockMvc.perform(delete("/api/usuarios/1")
                        .param("confirmar", "true"))
                .andExpect(status().isOk());
    }
}