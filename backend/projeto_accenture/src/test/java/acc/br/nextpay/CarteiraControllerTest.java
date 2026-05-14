package acc.br.nextpay;

import acc.br.nextpay.controller.CarteiraController;
import acc.br.nextpay.dto.CarteiraRequest;
import acc.br.nextpay.model.ContaCorrente;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.security.JwtUtil;
import acc.br.nextpay.service.TransacaoService;
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

import java.math.BigDecimal;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarteiraController.class)
@AutoConfigureMockMvc(addFilters = false)
class CarteiraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransacaoService transacaoService;

    @MockBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        var auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testSaldo() throws Exception {
        ContaCorrente conta = ContaCorrente.builder()
                .id(1L)
                .numeroConta("12345678")
                .saldo(BigDecimal.valueOf(100))
                .build();

        Mockito.when(transacaoService.buscarContaPorUsuario(1L)).thenReturn(conta);

        mockMvc.perform(get("/api/carteiras/saldo"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeposito() throws Exception {
        CarteiraRequest request = new CarteiraRequest();
        request.setValor(BigDecimal.TEN);

        ContaCorrente conta = ContaCorrente.builder().saldo(BigDecimal.TEN).build();
        Mockito.when(transacaoService.depositar(Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenReturn(conta);

        mockMvc.perform(post("/api/carteiras/deposito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testSaque() throws Exception {
        CarteiraRequest request = new CarteiraRequest();
        request.setValor(BigDecimal.TEN);

        ContaCorrente conta = ContaCorrente.builder().saldo(BigDecimal.ZERO).build();
        Mockito.when(transacaoService.sacar(Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenReturn(conta);

        mockMvc.perform(post("/api/carteiras/saque")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testPix() throws Exception {
        CarteiraRequest request = new CarteiraRequest();
        request.setChavePix("teste@teste.com");
        request.setValor(BigDecimal.TEN);

        Mockito.when(transacaoService.pix(Mockito.anyLong(), Mockito.anyString(), Mockito.any(BigDecimal.class)))
                .thenReturn("PIX realizado com sucesso.");

        mockMvc.perform(post("/api/carteiras/pix")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testTransferencia() throws Exception {
        CarteiraRequest request = new CarteiraRequest();
        request.setUsuarioDestinoId(2L);
        request.setValor(BigDecimal.TEN);

        Mockito.when(transacaoService.transferir(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenReturn("Transferência realizada com sucesso.");

        mockMvc.perform(post("/api/carteiras/transferencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testBuscarChavePix() throws Exception {
        Usuario mockUser = new Usuario();
        mockUser.setId(1L);
        mockUser.setNome("Teste");
        mockUser.setEmail("teste@teste.com");
        mockUser.setCpfCnpj("12345678901");

        Mockito.when(transacaoService.buscarUsuarioPorChavePix("teste@teste.com")).thenReturn(mockUser);

        mockMvc.perform(get("/api/carteiras/pix/buscar").param("chave", "teste@teste.com"))
                .andExpect(status().isOk());
    }
}
