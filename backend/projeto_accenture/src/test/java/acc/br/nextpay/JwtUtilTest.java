package acc.br.nextpay;

import acc.br.nextpay.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    // The secret must be at least 32 bytes for HMAC-SHA256
    private static final String SECRET =
            "nextpay-accenture-chave-secreta-muito-longa-para-seguranca-jwt-2024";
    private static final long EXPIRATION = 86400000L; // 24 h

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
    }

    @Test
    void deveGerarEValidarToken() {
        String token = jwtUtil.gerarToken(42L, "usuario@email.com");

        assertNotNull(token);
        assertTrue(jwtUtil.validarToken(token));
    }

    @Test
    void deveExtrairUsuarioIdDoToken() {
        Long usuarioId = 99L;
        String token = jwtUtil.gerarToken(usuarioId, "outro@email.com");

        Long extraido = jwtUtil.extrairUsuarioId(token);

        assertEquals(usuarioId, extraido);
    }

    @Test
    void deveRetornarFalsoParaTokenInvalido() {
        assertFalse(jwtUtil.validarToken("invalid.token.string"));
    }

    @Test
    void deveRetornarFalsoParaTokenVazio() {
        assertFalse(jwtUtil.validarToken(""));
    }

    @Test
    void deveRetornarFalsoParaTokenNulo() {
        assertFalse(jwtUtil.validarToken(null));
    }
}
