package acc.br.nextpay;

import acc.br.nextpay.config.SecurityConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    @InjectMocks
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveRetornarBCryptPasswordEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        Assertions.assertNotNull(encoder);
        Assertions.assertTrue(encoder instanceof BCryptPasswordEncoder);

        String senha = "admin";
        String hash = encoder.encode(senha);
        Assertions.assertTrue(encoder.matches(senha, hash));
    }

    @Test
    void deveExecutarSecurityFilterChainSemErros() throws Exception {
        // Criamos o mock do HttpSecurity
        HttpSecurity http = mock(HttpSecurity.class);

        // CORREÇÃO AQUI: Criamos o mock da classe concreta exata que o compilador exige no build()
        DefaultSecurityFilterChain mockChain = mock(DefaultSecurityFilterChain.class);

        // Mapeamos as chamadas fluídas retornando o próprio mock do HttpSecurity
        when(http.csrf(any())).thenReturn(http);
        when(http.headers(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);

        // Agora o compilador aceita, pois o tipo bate exatamente com o esperado
        when(http.build()).thenReturn(mockChain);

        // Executa o método da SecurityConfig
        Assertions.assertDoesNotThrow(() -> {
            SecurityFilterChain result = securityConfig.securityFilterChain(http);
            Assertions.assertNotNull(result);
        });

        // Garante que o fluxo passou por todas as configurações até o build
        verify(http, times(1)).build();
    }
}