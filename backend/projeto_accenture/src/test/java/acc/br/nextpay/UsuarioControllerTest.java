package acc.br.nextpay;

import acc.br.nextpay.controller.UsuarioController;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.security.JwtUtil;
import acc.br.nextpay.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @MockBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        var auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testListarTodos() throws Exception {
        Mockito.when(usuarioService.listarTodos()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    void testBuscarPorId() throws Exception {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNome("Jose");
        u.setEmail("jose@email.com");
        u.setCpfCnpj("12345678901");
        u.setSenha("hash");

        Mockito.when(usuarioService.buscarPorId(1L)).thenReturn(u);

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
        u.setId(1L);
        u.setNome("Novo Nome");
        u.setEmail("jose@email.com");
        u.setCpfCnpj("12345678901");
        u.setSenha("hash");

        Mockito.when(usuarioService.atualizarUsuario(Mockito.anyLong(), Mockito.anyLong(), any(Usuario.class)))
                .thenReturn(u);

        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(u)))
                .andExpect(status().isOk());
    }

    @Test
    void testExcluirConta() throws Exception {
        Mockito.doNothing().when(usuarioService).excluirConta(Mockito.anyLong(), Mockito.anyLong());

        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testConfirmarEmail() throws Exception {
        Mockito.doNothing().when(usuarioService).confirmarCodigoEmail(anyString(), anyString());

        mockMvc.perform(post("/api/usuarios/confirmar-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\",\"codigo\":\"123456\"}"))
                .andExpect(status().isOk());
    }
}
