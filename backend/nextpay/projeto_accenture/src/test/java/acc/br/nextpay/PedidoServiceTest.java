package acc.br.nextpay;

import acc.br.nextpay.dto.PedidoDTO;
import acc.br.nextpay.model.*;
import acc.br.nextpay.model.enums.NivelUsuario;
import acc.br.nextpay.model.enums.StatusPedido;
import acc.br.nextpay.model.enums.TipoTransacao;
import acc.br.nextpay.repository.ContaCorrenteRepository;
import acc.br.nextpay.repository.PedidoRepository;
import acc.br.nextpay.repository.ProdutoRepository;
import acc.br.nextpay.repository.UsuarioRepository;
import acc.br.nextpay.service.EmailService;
import acc.br.nextpay.service.PedidoService;
import acc.br.nextpay.service.TransacaoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class PedidoServiceTest {

    @InjectMocks
    private PedidoService pedidoService;

    @Mock private PedidoRepository pedidoRepository;
    @Mock private ProdutoRepository produtoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ContaCorrenteRepository contaCorrenteRepository;
    @Mock private TransacaoService transacaoService;
    @Mock private EmailService emailService;

    private Usuario comprador;
    private Usuario vendedor;
    private Produto produto;
    private ContaCorrente contaComprador;
    private ContaCorrente contaVendedor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(pedidoService, "emailService", emailService);

        // Setup Conta Comprador (ID: 10)
        contaComprador = new ContaCorrente();
        contaComprador.setId(10L);
        contaComprador.setSaldo(new BigDecimal("1000.00"));

        // Setup Conta Vendedor (ID: 20)
        contaVendedor = new ContaCorrente();
        contaVendedor.setId(20L);
        contaVendedor.setSaldo(new BigDecimal("500.00"));

        // Setup Comprador
        comprador = new Usuario();
        comprador.setId(1L);
        comprador.setNome("Comprador");
        comprador.setEmail("comprador@email.com");
        comprador.setPontuacao(100);
        comprador.setNivel(NivelUsuario.BRONZE); // Assumindo o Enum padrão do projeto
        comprador.setConta(contaComprador);

        // Setup Vendedor
        vendedor = new Usuario();
        vendedor.setId(2L);
        vendedor.setNome("Vendedor");
        vendedor.setEmail("vendedor@email.com");
        vendedor.setPontuacao(50);
        vendedor.setNivel(NivelUsuario.BRONZE);
        vendedor.setConta(contaVendedor);

        // Setup Produto
        produto = new Produto();
        produto.setId(100L);
        produto.setNome("Produto Teste");
        produto.setPreco(new BigDecimal("100.00"));
        produto.setQuantidadeEstoque(10);
        produto.setVendedor(vendedor);
    }

    @Test
    void deveComprarComSucessoUsandoMoedas() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(comprador));
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto));

        // Mock do findByIdWithLock ordenado (o código usa Math.min e Math.max para evitar deadlock)
        when(contaCorrenteRepository.findByIdWithLock(10L)).thenReturn(contaComprador);
        when(contaCorrenteRepository.findByIdWithLock(20L)).thenReturn(contaVendedor);

        Pedido pedidoFake = Pedido.builder()
                .id(1L).comprador(comprador).vendedor(vendedor).produto(produto)
                .quantidade(2).valorTotal(new BigDecimal("200.00")).status(StatusPedido.FINALIZADO)
                .dataPedido(LocalDateTime.now()).build();

        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoFake);

        PedidoDTO resultado = pedidoService.comprar(1L, 100L, 2, new BigDecimal("10.00"));

        Assertions.assertNotNull(resultado);
        Assertions.assertEquals("FINALIZADO", resultado.getStatus());
        verify(pedidoRepository, atLeastOnce()).save(any(Pedido.class));
    }

    @Test
    void deveReservarProdutoComSucesso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(comprador));
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto));

        Pedido pedidoReservado = Pedido.builder()
                .id(2L).comprador(comprador).vendedor(vendedor).produto(produto)
                .quantidade(1).valorTotal(new BigDecimal("100.00")).status(StatusPedido.RESERVADO)
                .dataPedido(LocalDateTime.now()).build();

        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoReservado);

        PedidoDTO resultado = pedidoService.reservar(1L, 100L, 1);

        Assertions.assertNotNull(resultado);
        Assertions.assertEquals("RESERVADO", resultado.getStatus());
    }

    @Test
    void deveConfirmarReservaComSucessoEDispararEmail() {
        Pedido pedidoReserva = Pedido.builder()
                .id(3L).comprador(comprador).vendedor(vendedor).produto(produto)
                .quantidade(1).valorTotal(new BigDecimal("100.00")).status(StatusPedido.RESERVADO)
                .build();

        when(pedidoRepository.findById(3L)).thenReturn(Optional.of(pedidoReserva));
        when(contaCorrenteRepository.findByIdWithLock(10L)).thenReturn(contaComprador);
        when(contaCorrenteRepository.findByIdWithLock(20L)).thenReturn(contaVendedor);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoReserva);

        // Modifica nível para forçar a cobertura do método notificarMudancaDeNivel
        comprador.setNivel(NivelUsuario.PRATA);

        PedidoDTO resultado = pedidoService.confirmarReserva(3L, 1L, BigDecimal.ZERO);

        Assertions.assertNotNull(resultado);
        Assertions.assertEquals("FINALIZADO", resultado.getStatus());
        verify(emailService, atLeastOnce()).enviarEmail(any(), any(), any());
    }

    @Test
    void deveCancelarReservaComSucesso() {
        Pedido pedidoReserva = Pedido.builder()
                .id(4L).comprador(comprador).vendedor(vendedor).produto(produto)
                .quantidade(2).status(StatusPedido.RESERVADO).build();

        when(pedidoRepository.findById(4L)).thenReturn(Optional.of(pedidoReserva));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoReserva);

        PedidoDTO resultado = pedidoService.cancelarReserva(4L);

        Assertions.assertNotNull(resultado);
        Assertions.assertEquals("CANCELADO", resultado.getStatus());
    }

    @Test
    void deveListarTodosOsPedidos() {
        when(pedidoRepository.findAll()).thenReturn(List.of(new Pedido()));
        List<Pedido> lista = pedidoService.listarTodos();
        Assertions.assertFalse(lista.isEmpty());
    }

    @Test
    void deveLancarExcecaoAoValidarQuantidadeInvalida() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(comprador));
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto));

        Assertions.assertThrows(RuntimeException.class, () -> {
            pedidoService.comprar(1L, 100L, 0, BigDecimal.ZERO);
        });
    }

    @Test
    void deveLancarExcecaoAoComprarProprioProduto() {
        // Define o comprador como dono do produto
        produto.setVendedor(comprador);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(comprador));
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto));

        Assertions.assertThrows(RuntimeException.class, () -> {
            pedidoService.comprar(1L, 100L, 1, BigDecimal.ZERO);
        });
    }

    @Test
    void deveLancarExcecaoAoConfirmarPedidoNaoReservado() {
        Pedido pedidoFinalizado = Pedido.builder().status(StatusPedido.FINALIZADO).build();
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoFinalizado));

        Assertions.assertThrows(RuntimeException.class, () -> {
            pedidoService.confirmarReserva(1L, 1L, BigDecimal.ZERO);
        });
    }

    @Test
    void deveLancarExcecaoAoCancelarPedidoNaoReservado() {
        Pedido pedidoFinalizado = Pedido.builder().status(StatusPedido.FINALIZADO).build();
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoFinalizado));

        Assertions.assertThrows(RuntimeException.class, () -> {
            pedidoService.cancelarReserva(1L);
        });
    }
}