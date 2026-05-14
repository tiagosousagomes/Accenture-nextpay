package acc.br.nextpay;

import acc.br.nextpay.controller.PedidoController;
import acc.br.nextpay.dto.PedidoDTO;
import acc.br.nextpay.security.JwtUtil;
import acc.br.nextpay.service.PedidoService;
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

import java.math.BigDecimal;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    @MockBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        var auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private PedidoDTO pedidoDTOFake() {
        return PedidoDTO.builder()
                .id(1L)
                .nomeComprador("Comprador")
                .nomeVendedor("Vendedor")
                .nomeProduto("Produto")
                .quantidade(1)
                .valorTotal(BigDecimal.TEN)
                .status("FINALIZADO")
                .build();
    }

    @Test
    void testComprar() throws Exception {
        Mockito.when(pedidoService.comprar(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt()))
                .thenReturn(pedidoDTOFake());

        mockMvc.perform(post("/api/pedidos/comprar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"produtoId\":1, \"quantidade\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    void testReservar() throws Exception {
        PedidoDTO reservado = PedidoDTO.builder()
                .id(1L).nomeComprador("Comprador").nomeVendedor("Vendedor")
                .nomeProduto("Produto").quantidade(1).valorTotal(BigDecimal.TEN)
                .status("RESERVADO").build();

        Mockito.when(pedidoService.reservar(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt()))
                .thenReturn(reservado);

        mockMvc.perform(post("/api/pedidos/reservar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"produtoId\":1, \"quantidade\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    void testConfirmarReserva() throws Exception {
        Mockito.when(pedidoService.confirmarReserva(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(pedidoDTOFake());

        mockMvc.perform(put("/api/pedidos/1/confirmar"))
                .andExpect(status().isOk());
    }

    @Test
    void testCancelarReserva() throws Exception {
        PedidoDTO cancelado = PedidoDTO.builder()
                .id(1L).nomeComprador("Comprador").nomeVendedor("Vendedor")
                .nomeProduto("Produto").quantidade(1).valorTotal(BigDecimal.TEN)
                .status("CANCELADO").build();

        Mockito.when(pedidoService.cancelarReserva(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(cancelado);

        mockMvc.perform(put("/api/pedidos/1/cancelar"))
                .andExpect(status().isOk());
    }

    @Test
    void testListarTodos() throws Exception {
        Mockito.when(pedidoService.listarTodos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk());
    }
}
