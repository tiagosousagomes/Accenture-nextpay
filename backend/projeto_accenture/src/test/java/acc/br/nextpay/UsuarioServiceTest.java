package acc.br.nextpay;

import acc.br.nextpay.exception.NegocioException;
import acc.br.nextpay.exception.RecursoNaoEncontradoException;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.repository.UsuarioRepository;
import acc.br.nextpay.service.EmailService;
import acc.br.nextpay.service.UsuarioService;
import acc.br.nextpay.service.ViaCepService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @InjectMocks
    private UsuarioService service;

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ViaCepService viaCepService;

    @Mock
    private EmailService emailService;

    // ------------------------------------------------------------------ //
    //  cadastrarUsuario
    // ------------------------------------------------------------------ //

    @Test
    void deveCadastrarComSucesso() {
        Usuario u = new Usuario();
        u.setEmail("novo@email.com");
        u.setCpfCnpj("00000000001");

        Mockito.when(repository.existsByEmail(anyString())).thenReturn(false);
        Mockito.when(repository.existsByCpfCnpj(anyString())).thenReturn(false);
        Mockito.when(passwordEncoder.encode(any())).thenReturn("hash");
        Mockito.when(viaCepService.buscarEnderecoPorCep(anyString())).thenReturn(null);
        Mockito.when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario salvo = service.cadastrarUsuario(u, "01001000", "10");

        assertNotNull(salvo);
        Mockito.verify(repository).save(any());
    }

    @Test
    void deveLancarErroEmailDuplicado() {
        Usuario u = new Usuario();
        u.setEmail("jaexiste@email.com");

        Mockito.when(repository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(NegocioException.class, () -> service.cadastrarUsuario(u, "123", "1"));
    }

    @Test
    void deveLancarErroCpfCnpjDuplicado() {
        Usuario u = new Usuario();
        u.setEmail("novo@email.com");
        u.setCpfCnpj("11111111111");

        Mockito.when(repository.existsByEmail(anyString())).thenReturn(false);
        Mockito.when(repository.existsByCpfCnpj(anyString())).thenReturn(true);

        assertThrows(NegocioException.class, () -> service.cadastrarUsuario(u, "01001000", "1"));
    }

    // ------------------------------------------------------------------ //
    //  login
    // ------------------------------------------------------------------ //

    @Test
    void deveLogarComSucesso() {
        Usuario u = new Usuario();
        u.setEmail("user@email.com");
        u.setSenha("hashado");

        Mockito.when(repository.findByEmail("user@email.com")).thenReturn(Optional.of(u));
        Mockito.when(passwordEncoder.matches("senha123", "hashado")).thenReturn(true);

        Usuario resultado = service.login("user@email.com", "senha123");

        assertNotNull(resultado);
        assertEquals("user@email.com", resultado.getEmail());
    }

    @Test
    void deveLancarErroLoginSenhaErrada() {
        Usuario u = new Usuario();
        u.setEmail("user@email.com");
        u.setSenha("hashado");

        Mockito.when(repository.findByEmail("user@email.com")).thenReturn(Optional.of(u));
        Mockito.when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(NegocioException.class, () -> service.login("user@email.com", "senhaErrada"));
    }

    @Test
    void deveLancarErroLoginUsuarioNaoEncontrado() {
        Mockito.when(repository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(NegocioException.class, () -> service.login("inexistente@email.com", "qualquer"));
    }

    // ------------------------------------------------------------------ //
    //  buscarPorId
    // ------------------------------------------------------------------ //

    @Test
    void deveBuscarPorIdComSucesso() {
        Usuario u = new Usuario();
        u.setId(1L);

        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(u));

        Usuario resultado = service.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void deveLancarErroBuscarPorIdNaoEncontrado() {
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorId(99L));
    }

    // ------------------------------------------------------------------ //
    //  excluirConta
    // ------------------------------------------------------------------ //

    @Test
    void deveExcluirContaComSucesso() {
        Usuario u = new Usuario();
        u.setId(1L);

        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(u));

        assertDoesNotThrow(() -> service.excluirConta(1L, 1L));
        Mockito.verify(repository).delete(u);
    }

    @Test
    void deveLancarErroExcluirContaOutroUsuario() {
        assertThrows(NegocioException.class, () -> service.excluirConta(1L, 2L));
    }

    // ------------------------------------------------------------------ //
    //  atualizarUsuario
    // ------------------------------------------------------------------ //

    @Test
    void deveAtualizarUsuarioComSucesso() {
        Usuario existente = new Usuario();
        existente.setId(1L);
        existente.setNome("Antigo");

        Usuario atualizado = new Usuario();
        atualizado.setNome("Novo Nome");

        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(existente));
        Mockito.when(repository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = service.atualizarUsuario(1L, 1L, atualizado);

        assertEquals("Novo Nome", resultado.getNome());
    }

    @Test
    void deveLancarErroAtualizarOutroUsuario() {
        assertThrows(NegocioException.class,
                () -> service.atualizarUsuario(1L, 2L, new Usuario()));
    }

    // ------------------------------------------------------------------ //
    //  confirmarCodigoEmail
    // ------------------------------------------------------------------ //

    @Test
    void deveConfirmarEmailComSucesso() {
        Usuario u = new Usuario();
        u.setEmail("user@email.com");
        u.setEmailConfirmado(false);
        u.setTokenConfirmacao("123456");

        Mockito.when(repository.findByEmail("user@email.com")).thenReturn(Optional.of(u));
        Mockito.when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> service.confirmarCodigoEmail("user@email.com", "123456"));

        assertTrue(u.isEmailConfirmado());
        assertNull(u.getTokenConfirmacao());
    }

    @Test
    void deveLancarErroConfirmarEmailJaConfirmado() {
        Usuario u = new Usuario();
        u.setEmailConfirmado(true);

        Mockito.when(repository.findByEmail("user@email.com")).thenReturn(Optional.of(u));

        assertThrows(NegocioException.class,
                () -> service.confirmarCodigoEmail("user@email.com", "123456"));
    }

    @Test
    void deveLancarErroConfirmarEmailCodigoInvalido() {
        Usuario u = new Usuario();
        u.setEmailConfirmado(false);
        u.setTokenConfirmacao("654321");

        Mockito.when(repository.findByEmail("user@email.com")).thenReturn(Optional.of(u));

        assertThrows(NegocioException.class,
                () -> service.confirmarCodigoEmail("user@email.com", "000000"));
    }

    // ------------------------------------------------------------------ //
    //  listarTodos
    // ------------------------------------------------------------------ //

    @Test
    void deveListarTodosOsUsuarios() {
        Mockito.when(repository.findAll()).thenReturn(List.of(new Usuario(), new Usuario()));

        List<Usuario> lista = service.listarTodos();

        assertEquals(2, lista.size());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaUsuarios() {
        Mockito.when(repository.findAll()).thenReturn(Collections.emptyList());

        assertTrue(service.listarTodos().isEmpty());
    }
}
