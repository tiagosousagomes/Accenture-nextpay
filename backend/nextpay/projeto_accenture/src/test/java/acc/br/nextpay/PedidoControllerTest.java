package acc.br.nextpay;

import acc.br.nextpay.controller.PedidoController;
import acc.br.nextpay.dto.PedidoDTO;
import acc.br.nextpay.model.Pedido;
import acc.br.nextpay.service.PedidoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    private SecurityContext originalSecurityContext;
    private PedidoDTO pedidoDTOMock;

    @BeforeEach
    void setUp() {
        originalSecurityContext = SecurityContextHolder.getContext();
        // Criamos um mock do DTO isolando qualquer problema de construtor ou visibilidade
        pedidoDTOMock = Mockito.mock(PedidoDTO.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalSecurityContext);
    }

    private void mockSecurityPrincipal(Object principal) {
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Authentication authentication = Mockito.mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);
    }

    // ==========================================
    // TESTES DO MÉTODO: comprar
    // ==========================================

    @Test
    void testComprarComUsuarioIdNoRequest() throws Exception {
        mockSecurityPrincipal("anonymousUser");

        when(pedidoService.comprar(eq(1L), eq(10L), eq(2), any(BigDecimal.class)))
                .thenReturn(pedidoDTOMock);

        mockMvc.perform(post("/api/pedidos/comprar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"compradorId\":1, \"produtoId\":10, \"quantidade\":2, \"moedasUsadas\":0}"))
                .andExpect(status().isOk());
    }

    @Test
    void testComprarComUsuarioAutenticadoComoLong() throws Exception {
        mockSecurityPrincipal(5L);

        when(pedidoService.comprar(eq(5L), eq(10L), eq(2), any(BigDecimal.class)))
                .thenReturn(pedidoDTOMock);

        mockMvc.perform(post("/api/pedidos/comprar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"compradorId\":1, \"produtoId\":10, \"quantidade\":2, \"moedasUsadas\":0}"))
                .andExpect(status().isOk());
    }

    @Test
    void testComprarComUsuarioAutenticadoComoStringId() throws Exception {
        mockSecurityPrincipal("99");

        when(pedidoService.comprar(eq(99L), eq(10L), eq(2), any(BigDecimal.class)))
                .thenReturn(pedidoDTOMock);

        mockMvc.perform(post("/api/pedidos/comprar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"compradorId\":1, \"produtoId\":10, \"quantidade\":2, \"moedasUsadas\":0}"))
                .andExpect(status().isOk());
    }

    @Test
    void testComprarWhenContextSecurityThrowsException() throws Exception {
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenThrow(new RuntimeException("Erro de segurança"));
        SecurityContextHolder.setContext(securityContext);

        when(pedidoService.comprar(eq(1L), eq(10L), eq(2), any(BigDecimal.class)))
                .thenReturn(pedidoDTOMock);

        mockMvc.perform(post("/api/pedidos/comprar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"compradorId\":1, \"produtoId\":10, \"quantidade\":2, \"moedasUsadas\":0}"))
                .andExpect(status().isOk());
    }

    // ==========================================
    // TESTES DO MÉTODO: reservar
    // ==========================================

    @Test
    void testReservar() throws Exception {
        when(pedidoService.reservar(eq(1L), eq(20L), eq(5)))
                .thenReturn(pedidoDTOMock);

        mockMvc.perform(post("/api/pedidos/reservar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"compradorId\":1, \"produtoId\":20, \"quantidade\":5}"))
                .andExpect(status().isOk());
    }

    // ==========================================
    // TESTES DO MÉTODO: confirmarReserva
    // ==========================================

    @Test
    void testConfirmarReservaComRequestCompleto() throws Exception {
        mockSecurityPrincipal("anonymousUser");

        when(pedidoService.confirmarReserva(eq(100L), eq(1L), any(BigDecimal.class)))
                .thenReturn(pedidoDTOMock);

        mockMvc.perform(put("/api/pedidos/100/confirmar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"compradorId\":1, \"moedasUsadas\":10.50}"))
                .andExpect(status().isOk());
    }

    @Test
    void testConfirmarReservaComRequestNulo() throws Exception {
        mockSecurityPrincipal("anonymousUser");

        when(pedidoService.confirmarReserva(eq(100L), eq(null), eq(null)))
                .thenReturn(pedidoDTOMock);

        mockMvc.perform(put("/api/pedidos/100/confirmar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ==========================================
    // TESTES DO MÉTODO: cancelarReserva
    // ==========================================

    @Test
    void testCancelarReserva() throws Exception {
        when(pedidoService.cancelarReserva(eq(100L)))
                .thenReturn(pedidoDTOMock);

        mockMvc.perform(put("/api/pedidos/100/cancelar"))
                .andExpect(status().isOk());
    }

    // ==========================================
    // TESTES DO MÉTODO: listarTodos
    // ==========================================

    @Test
    void testListarTodos() throws Exception {
        when(pedidoService.listarTodos()).thenReturn(List.of(new Pedido()));

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk());
    }
}