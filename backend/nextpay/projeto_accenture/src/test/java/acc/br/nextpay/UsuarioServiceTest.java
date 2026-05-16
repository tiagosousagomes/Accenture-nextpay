package acc.br.nextpay;

import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.repository.UsuarioRepository;
import acc.br.nextpay.service.UsuarioService;
import acc.br.nextpay.service.ViaCepService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @InjectMocks
    private UsuarioService service;
    @Mock
    private UsuarioRepository repository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ViaCepService viaCepService;

    @Test
    void deveCadastrarComSucesso() {
        Usuario u = new Usuario(); u.setEmail("novo@email.com");
        Mockito.when(repository.existsByEmail(anyString())).thenReturn(false);
        Mockito.when(passwordEncoder.encode(any())).thenReturn("hash");

        service.cadastrarUsuario(u, "12345678", "10");
        Mockito.verify(repository).save(any());
    }

    @Test
    void deveLancarErroEmailDuplicado() {
        Usuario u = new Usuario(); u.setEmail("jaexiste@email.com");
        Mockito.when(repository.existsByEmail(anyString())).thenReturn(true);
        Assertions.assertThrows(RuntimeException.class, () -> service.cadastrarUsuario(u, "123", "1"));
    }
}