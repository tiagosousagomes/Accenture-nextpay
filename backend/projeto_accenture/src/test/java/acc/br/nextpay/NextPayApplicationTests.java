package acc.br.nextpay;

import acc.br.nextpay.ai.AssistenteRegrasNegocio;
import acc.br.nextpay.ai.DocumentChunk;
import acc.br.nextpay.model.*;
import acc.br.nextpay.model.enums.*;
import acc.br.nextpay.repository.*;
import acc.br.nextpay.security.JwtUtil;
import acc.br.nextpay.service.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NextPayApplicationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private TransacaoService transacaoService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private JwtUtil jwtUtil;

    @MockitoBean private UsuarioRepository usuarioRepository;
    @MockitoBean private TransacaoRepository transacaoRepository;
    @MockitoBean private ContaCorrenteRepository contaCorrenteRepository;
    @MockitoBean private PasswordEncoder passwordEncoder;
    @MockitoBean private AssistenteRegrasNegocio assistente;
    @MockitoBean private ProdutoRepository produtoRepository;
    @MockitoBean private PedidoRepository pedidoRepository;
    @MockitoBean private ViaCepService viaCepService;

    @Test
    void coberturaControllers() throws Exception {

        Usuario u = new Usuario();
        u.setId(1L);
        u.setNome("User");
        u.setEmail("a@a.com");
        u.setSenha("senha_hash");

        // Generate a real JWT so JwtFilter can validate it and set the authentication principal
        String token = jwtUtil.gerarToken(1L, "a@a.com");

        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        mockMvc.perform(get("/api/usuarios/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Mockito.when(usuarioRepository.findByEmail("a@a.com")).thenReturn(Optional.of(u));
        Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.eq("senha_hash"))).thenReturn(true);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@a.com\",\"senha\":\"123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void coberturaServiceExcecoes() {

        Usuario u = new Usuario();
        u.setId(1L);

        u.setConta(ContaCorrente.builder().saldo(BigDecimal.ZERO).limite(BigDecimal.ZERO).build());

        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        Assertions.assertThrows(RuntimeException.class, () -> {
            transacaoService.sacar(1L, BigDecimal.TEN);
        }, "Deveria lançar erro de saldo insuficiente");
    }

    @Test
    void coberturaModels() {
        Usuario u = new Usuario();
        u.setNome("Teste");
        ContaCorrente cc = ContaCorrente.builder().saldo(BigDecimal.ONE).build();
        u.setConta(cc);

        Produto p = new Produto();
        p.setNome("Produto");
        p.setVendedor(u);

        Transacao t = Transacao.builder().valor(BigDecimal.TEN).build();

        Assertions.assertEquals("Teste", u.getNome());
        Assertions.assertEquals(BigDecimal.ONE, cc.getSaldo());
        Assertions.assertNotNull(p.getVendedor());
        Assertions.assertEquals(BigDecimal.TEN, t.getValor());

        DocumentChunk chunk = new DocumentChunk("c", List.of(1f));
        Assertions.assertEquals("c", chunk.content());
    }

    @Test
    void testeEnums() {
        for (TipoUsuario t : TipoUsuario.values()) Assertions.assertNotNull(TipoUsuario.valueOf(t.name()));
        for (StatusPedido s : StatusPedido.values()) Assertions.assertNotNull(StatusPedido.valueOf(s.name()));
    }
}