package acc.br.nextpay;

import acc.br.nextpay.model.*;
import acc.br.nextpay.repository.*;
import acc.br.nextpay.service.TransacaoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @InjectMocks
    private TransacaoService transacaoService;

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ContaCorrenteRepository contaCorrenteRepository;
    @Mock private TransacaoRepository transacaoRepository;

    // --- SUCESSOS (O que faltava para subir a cobertura) ---

    @Test
    void testSacarComSucesso() {
        Usuario u = new Usuario();
        u.setId(1L);
        // Saldo de 100, sacando 50
        u.setConta(ContaCorrente.builder().saldo(BigDecimal.valueOf(100)).limite(BigDecimal.ZERO).build());

        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        transacaoService.sacar(1L, BigDecimal.valueOf(50));

        assertEquals(BigDecimal.valueOf(50), u.getConta().getSaldo());
    }

    @Test
    void testPixComSucesso() {
        Usuario origem = new Usuario(); origem.setId(1L);
        origem.setConta(ContaCorrente.builder().saldo(BigDecimal.valueOf(100)).limite(BigDecimal.ZERO).build());

        Usuario destino = new Usuario(); destino.setId(2L); destino.setNome("Destino");
        destino.setConta(ContaCorrente.builder().saldo(BigDecimal.ZERO).build());

        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.of(origem));
        Mockito.when(usuarioRepository.findByEmail("pix@teste.com")).thenReturn(Optional.of(destino));

        String msg = transacaoService.pix(1L, "pix@teste.com", BigDecimal.valueOf(50));

        assertEquals("PIX realizado com sucesso.", msg);
        assertEquals(BigDecimal.valueOf(50), origem.getConta().getSaldo());
        assertEquals(BigDecimal.valueOf(50), destino.getConta().getSaldo());
    }

    @Test
    void testTransferenciaComSucesso() {
        Usuario origem = new Usuario(); origem.setId(1L);
        origem.setConta(ContaCorrente.builder().saldo(BigDecimal.valueOf(100)).limite(BigDecimal.ZERO).build());

        Usuario destino = new Usuario(); destino.setId(2L); destino.setNome("Destino");
        destino.setConta(ContaCorrente.builder().saldo(BigDecimal.ZERO).build());

        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.of(origem));
        Mockito.when(usuarioRepository.findById(2L)).thenReturn(Optional.of(destino));

        String msg = transacaoService.transferir(1L, 2L, BigDecimal.valueOf(50));

        assertEquals("Transferência realizada com sucesso.", msg);
    }

    @Test
    void testListarTransacoes() {
        Mockito.when(transacaoRepository.findByUsuarioIdOrderByDataDesc(1L))
                .thenReturn(Collections.singletonList(new Transacao()));

        assertFalse(transacaoService.listarTransacoesPorUsuario(1L).isEmpty());
    }

    // --- ERROS ADICIONAIS (Para fechar 100%) ---

    @Test
    void testUsuarioNaoEncontrado() {
        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> transacaoService.depositar(1L, BigDecimal.TEN));
    }

    @Test
    void testTransferenciaParaPropriaContaErro() {
        // Cobre: if (usuarioOrigemId.equals(usuarioDestinoId))
        assertThrows(RuntimeException.class, () ->
                transacaoService.transferir(1L, 1L, BigDecimal.TEN)
        );
    }

    @Test
    void testChavePixNaoEncontrada() {
        Mockito.when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());
        Mockito.when(usuarioRepository.findByCpfCnpj(any())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> transacaoService.buscarUsuarioPorChavePix("inexistente"));
    }

    // --- TESTES ORIGINAIS (Mantidos) ---

    @Test
    void testDepositarComSucesso() {
        Usuario u = new Usuario(); u.setId(1L);
        u.setConta(ContaCorrente.builder().saldo(BigDecimal.ZERO).build());
        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        transacaoService.depositar(1L, BigDecimal.valueOf(100));
        assertEquals(BigDecimal.valueOf(100), u.getConta().getSaldo());
    }

    @Test
    void testPixParaPropriaContaDeveFalhar() {
        Usuario u = new Usuario(); u.setId(1L); u.setEmail("t@t.com");
        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        Mockito.when(usuarioRepository.findByEmail("t@t.com")).thenReturn(Optional.of(u));

        assertThrows(RuntimeException.class, () -> transacaoService.pix(1L, "t@t.com", BigDecimal.TEN));
    }
}