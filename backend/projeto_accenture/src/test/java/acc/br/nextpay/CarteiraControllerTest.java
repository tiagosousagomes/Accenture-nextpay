package acc.br.nextpay;

import acc.br.nextpay.controller.CarteiraController;
import acc.br.nextpay.dto.CarteiraRequest;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.repository.ContaCorrenteRepository;
import acc.br.nextpay.service.TransacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
    private ContaCorrenteRepository contaCorrenteRepository;

    @MockBean
    private TransacaoService transacaoService;

    @Test
    void testListarSaldos() throws Exception {
        Mockito.when(contaCorrenteRepository.findAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/carteiras/saldos"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeposito() throws Exception {
        CarteiraRequest request = new CarteiraRequest();
        request.setUsuarioId(1L);
        request.setValor(BigDecimal.TEN);

        mockMvc.perform(post("/api/carteiras/deposito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testSaque() throws Exception {
        CarteiraRequest request = new CarteiraRequest();
        request.setUsuarioId(1L);
        request.setValor(BigDecimal.TEN);

        mockMvc.perform(post("/api/carteiras/saque")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testPix() throws Exception {
        CarteiraRequest request = new CarteiraRequest();
        request.setUsuarioOrigemId(1L);
        request.setChavePix("teste@teste.com");
        request.setValor(BigDecimal.TEN);

        Mockito.when(transacaoService.pix(Mockito.anyLong(), Mockito.anyString(), Mockito.any(BigDecimal.class)))
                .thenReturn("Sucesso");

        mockMvc.perform(post("/api/carteiras/pix")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testTransferencia() throws Exception {
        CarteiraRequest request = new CarteiraRequest();
        request.setUsuarioOrigemId(1L);
        request.setUsuarioDestinoId(2L);
        request.setValor(BigDecimal.TEN);

        Mockito.when(transacaoService.transferir(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(BigDecimal.class)))
                .thenReturn("Sucesso");

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