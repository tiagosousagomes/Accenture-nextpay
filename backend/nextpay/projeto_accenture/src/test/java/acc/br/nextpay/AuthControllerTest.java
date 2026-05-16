package acc.br.nextpay;

import acc.br.nextpay.controller.AuthController;
import acc.br.nextpay.dto.LoginRequest;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Configura o MockMvc de forma autônoma (Standalone) para testar apenas o Controller de forma ultra rápida
        this.mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    void deveRealizarLoginComSucesso() throws Exception {
        // GIVEN
        LoginRequest request = new LoginRequest();
        request.setEmail("usuario@nextpay.com");
        request.setSenha("senha123");

        Usuario usuarioFake = new Usuario();
        usuarioFake.setId(1L);
        usuarioFake.setNome("Pedro Neto");
        usuarioFake.setEmail("usuario@nextpay.com");

        when(usuarioService.login("usuario@nextpay.com", "senha123")).thenReturn(usuarioFake);

        // WHEN & THEN
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Login realizado com sucesso."))
                .andExpect(jsonPath("$.usuarioId").value(1))
                .andExpect(jsonPath("$.nome").value("Pedro Neto"))
                .andExpect(jsonPath("$.email").value("usuario@nextpay.com"));
    }

    @Test
    void deveLancarExcecaoQuandoCredenciaisForemInvalidas() throws Exception {
        // GIVEN
        LoginRequest request = new LoginRequest();
        request.setEmail("errado@nextpay.com");
        request.setSenha("senhaErrada");

        // Configura o mock do service para lançar a exceção esperada
        when(usuarioService.login("errado@nextpay.com", "senhaErrada"))
                .thenThrow(new RuntimeException("Credenciais inválidas."));

        // WHEN & THEN
        // Como o MockMvc standalone repassa a exceção pura embrulhada em uma ServletException,
        // nós capturamos ela para garantir que a linha do controller foi executada e testada.
        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        });
    }

    @Test
    void deveRealizarLogoutComSucesso() throws Exception {
        // WHEN & THEN
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Logout realizado com sucesso."));
    }
}