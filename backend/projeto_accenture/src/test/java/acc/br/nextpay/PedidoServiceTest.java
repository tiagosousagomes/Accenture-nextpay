package acc.br.nextpay;

import acc.br.nextpay.dto.PedidoDTO;
import acc.br.nextpay.exception.NegocioException;
import acc.br.nextpay.model.*;
import acc.br.nextpay.model.enums.StatusPedido;
import acc.br.nextpay.repository.*;
import acc.br.nextpay.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @InjectMocks
    private PedidoService pedidoService;

    @Mock private PedidoRepository pedidoRepository;
    @Mock private ProdutoRepository produtoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ContaCorrenteRepository contaCorrenteRepository;
    @Mock private TransacaoService transacaoService;

    // ------------------------------------------------------------------ //
    //  comprar — happy path
    // ------------------------------------------------------------------ //

    @Test
    void testComprarComSucesso() {
        Usuario comprador = new Usuario();
        comprador.setId(1L);
        comprador.setNome("Comprador");
        ContaCorrente contaComprador = ContaCorrente.builder()
                .id(1L).saldo(BigDecimal.valueOf(1000)).build();
        comprador.setConta(contaComprador);

        Usuario vendedor = new Usuario();
        vendedor.setId(2L);
        vendedor.setNome("Vendedor");
        ContaCorrente contaVendedor = ContaCorrente.builder()
                .id(2L).saldo(BigDecimal.ZERO).build();
        vendedor.setConta(contaVendedor);

        Produto produto = new Produto();
        produto.setId(10L);
        produto.setNome("Teclado");
        produto.setPreco(BigDecimal.valueOf(100));
        produto.setQuantidadeEstoque(10);
        produto.setVendedor(vendedor);

        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.of(comprador));
        Mockito.when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));
        Mockito.when(contaCorrenteRepository.findByIdWithLock(1L)).thenReturn(Optional.of(contaComprador));
        Mockito.when(contaCorrenteRepository.findByIdWithLock(2L)).thenReturn(Optional.of(contaVendedor));

        Pedido pedidoFake = Pedido.builder()
                .id(1L).comprador(comprador).vendedor(vendedor).produto(produto)
                .quantidade(1).valorTotal(BigDecimal.valueOf(100)).status(StatusPedido.FINALIZADO)
                .build();
        Mockito.when(pedidoRepository.save(any())).thenReturn(pedidoFake);
        Mockito.when(usuarioRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        PedidoDTO result = pedidoService.comprar(1L, 10L, 1);

        assertNotNull(result);
        assertEquals("FINALIZADO", result.getStatus());
        Mockito.verify(transacaoService, Mockito.times(2))
                .registrarTransacao(any(), any(), any(), any());
    }

    // ------------------------------------------------------------------ //
    //  reservar — happy path
    // ------------------------------------------------------------------ //

    @Test
    void testReservarComSucesso() {
        Usuario comprador = new Usuario();
        comprador.setId(1L);
        comprador.setNome("C");
        ContaCorrente contaComprador = ContaCorrente.builder()
                .id(1L).saldo(BigDecimal.valueOf(500)).build();
        comprador.setConta(contaComprador);

        Usuario vendedor = new Usuario();
        vendedor.setId(2L);
        vendedor.setNome("V");
        ContaCorrente contaVendedor = ContaCorrente.builder()
                .id(2L).saldo(BigDecimal.ZERO).build();
        vendedor.setConta(contaVendedor);

        Produto produto = new Produto();
        produto.setId(10L);
        produto.setNome("Mouse");
        produto.setPreco(BigDecimal.valueOf(50));
        produto.setQuantidadeEstoque(5);
        produto.setVendedor(vendedor);

        Pedido pedidoReservado = Pedido.builder()
                .id(1L).comprador(comprador).vendedor(vendedor).produto(produto)
                .quantidade(1).valorTotal(BigDecimal.valueOf(50)).status(StatusPedido.RESERVADO)
                .build();

        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.of(comprador));
        Mockito.when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));
        Mockito.when(pedidoRepository.save(any())).thenReturn(pedidoReservado);

        PedidoDTO result = pedidoService.reservar(1L, 10L, 1);

        assertNotNull(result);
        assertEquals("RESERVADO", result.getStatus());
    }

    // ------------------------------------------------------------------ //
    //  confirmarReserva — happy path (original test kept)
    // ------------------------------------------------------------------ //

    @Test
    void testReservarEConfirmarReserva() {
        Usuario comprador = new Usuario();
        comprador.setId(1L);
        comprador.setNome("C");
        ContaCorrente contaComprador = ContaCorrente.builder()
                .id(1L).saldo(BigDecimal.valueOf(500)).build();
        comprador.setConta(contaComprador);

        Usuario vendedor = new Usuario();
        vendedor.setId(2L);
        vendedor.setNome("V");
        ContaCorrente contaVendedor = ContaCorrente.builder()
                .id(2L).saldo(BigDecimal.ZERO).build();
        vendedor.setConta(contaVendedor);

        Produto produto = new Produto();
        produto.setNome("Produto X");
        produto.setVendedor(vendedor);
        produto.setPreco(BigDecimal.valueOf(50));
        produto.setQuantidadeEstoque(10);

        Pedido pedidoReservado = Pedido.builder()
                .id(1L).comprador(comprador).vendedor(vendedor).produto(produto)
                .quantidade(1).valorTotal(BigDecimal.valueOf(50)).status(StatusPedido.RESERVADO)
                .build();

        Mockito.when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoReservado));
        Mockito.when(contaCorrenteRepository.findByIdWithLock(1L)).thenReturn(Optional.of(contaComprador));
        Mockito.when(contaCorrenteRepository.findByIdWithLock(2L)).thenReturn(Optional.of(contaVendedor));
        Pedido pedidoFinalizado = Pedido.builder()
                .id(pedidoReservado.getId())
                .comprador(comprador).vendedor(vendedor).produto(produto)
                .quantidade(pedidoReservado.getQuantidade())
                .valorTotal(pedidoReservado.getValorTotal())
                .status(StatusPedido.FINALIZADO)
                .build();
        Mockito.when(pedidoRepository.save(any())).thenReturn(pedidoFinalizado);
        Mockito.when(usuarioRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        PedidoDTO result = pedidoService.confirmarReserva(1L, comprador.getId());

        assertEquals("FINALIZADO", result.getStatus());
    }

    // ------------------------------------------------------------------ //
    //  cancelarReserva — happy path
    // ------------------------------------------------------------------ //

    @Test
    void testCancelarReservaComSucesso() {
        Usuario comprador = new Usuario();
        comprador.setId(1L);
        comprador.setNome("C");

        Usuario vendedor = new Usuario();
        vendedor.setId(2L);
        vendedor.setNome("V");

        Produto produto = new Produto();
        produto.setId(5L);
        produto.setNome("Produto Y");
        produto.setQuantidadeEstoque(3);
        produto.setVendedor(vendedor);

        Pedido pedido = Pedido.builder()
                .id(1L).comprador(comprador).vendedor(vendedor).produto(produto)
                .quantidade(2).valorTotal(BigDecimal.valueOf(40)).status(StatusPedido.RESERVADO)
                .build();

        Pedido cancelado = Pedido.builder()
                .id(pedido.getId())
                .comprador(comprador).vendedor(vendedor).produto(produto)
                .quantidade(pedido.getQuantidade())
                .valorTotal(pedido.getValorTotal())
                .status(StatusPedido.CANCELADO)
                .build();

        Mockito.when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        Mockito.when(pedidoRepository.save(any())).thenReturn(cancelado);
        Mockito.when(produtoRepository.save(any())).thenReturn(produto);

        PedidoDTO result = pedidoService.cancelarReserva(1L, 1L);

        assertEquals("CANCELADO", result.getStatus());
        assertEquals(5, produto.getQuantidadeEstoque()); // estoque devolvido: 3 + 2
    }

    // ------------------------------------------------------------------ //
    //  cancelarReserva — comprador errado
    // ------------------------------------------------------------------ //

    @Test
    void testCancelarReservaOutroUsuarioFalha() {
        Usuario comprador = new Usuario();
        comprador.setId(1L);

        Pedido pedido = Pedido.builder()
                .id(1L).comprador(comprador).status(StatusPedido.RESERVADO)
                .build();

        Mockito.when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(NegocioException.class, () -> pedidoService.cancelarReserva(1L, 99L));
    }

    // ------------------------------------------------------------------ //
    //  confirmarReserva — status errado
    // ------------------------------------------------------------------ //

    @Test
    void testConfirmarReservaStatusErradoFalha() {
        Usuario comprador = new Usuario();
        comprador.setId(1L);

        Pedido pedido = Pedido.builder()
                .id(1L).comprador(comprador).status(StatusPedido.FINALIZADO)
                .build();

        Mockito.when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(NegocioException.class, () -> pedidoService.confirmarReserva(1L, 1L));
    }

    // ------------------------------------------------------------------ //
    //  validarPedido — quantidade zero
    // ------------------------------------------------------------------ //

    @Test
    void testValidarPedidoQuantidadeZeroFalha() {
        Usuario comprador = new Usuario();
        comprador.setId(1L);

        Usuario vendedor = new Usuario();
        vendedor.setId(2L);

        Produto produto = new Produto();
        produto.setVendedor(vendedor);
        produto.setQuantidadeEstoque(10);

        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.of(comprador));
        Mockito.when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));

        assertThrows(NegocioException.class, () -> pedidoService.comprar(1L, 10L, 0));
    }

    // ------------------------------------------------------------------ //
    //  validarPedido — estoque insuficiente
    // ------------------------------------------------------------------ //

    @Test
    void testValidarPedidoEstoqueInsuficienteFalha() {
        Usuario comprador = new Usuario();
        comprador.setId(1L);

        Usuario vendedor = new Usuario();
        vendedor.setId(2L);

        Produto produto = new Produto();
        produto.setVendedor(vendedor);
        produto.setQuantidadeEstoque(1);

        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.of(comprador));
        Mockito.when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));

        assertThrows(NegocioException.class, () -> pedidoService.comprar(1L, 10L, 5));
    }

    // ------------------------------------------------------------------ //
    //  comprar — saldo insuficiente (original kept)
    // ------------------------------------------------------------------ //

    @Test
    void testErroSaldoInsuficiente() {
        Usuario comprador = new Usuario();
        comprador.setId(1L);
        ContaCorrente contaComprador = ContaCorrente.builder()
                .id(1L).saldo(BigDecimal.ZERO).limite(BigDecimal.ZERO).build();
        comprador.setConta(contaComprador);

        Usuario vendedor = new Usuario();
        vendedor.setId(2L);
        ContaCorrente contaVendedor = ContaCorrente.builder()
                .id(2L).saldo(BigDecimal.ZERO).build();
        vendedor.setConta(contaVendedor);

        Produto produto = new Produto();
        produto.setId(10L);
        produto.setPreco(BigDecimal.valueOf(100));
        produto.setQuantidadeEstoque(10);
        produto.setVendedor(vendedor);

        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.of(comprador));
        Mockito.when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));
        Mockito.when(contaCorrenteRepository.findByIdWithLock(1L)).thenReturn(Optional.of(contaComprador));
        Mockito.when(contaCorrenteRepository.findByIdWithLock(2L)).thenReturn(Optional.of(contaVendedor));

        assertThrows(RuntimeException.class, () -> pedidoService.comprar(1L, 10L, 1));
    }

    // ------------------------------------------------------------------ //
    //  comprar — próprio produto (original kept)
    // ------------------------------------------------------------------ //

    @Test
    void testErroComprarProprioProduto() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Produto produto = new Produto();
        produto.setVendedor(usuario);
        produto.setQuantidadeEstoque(5);

        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        Mockito.when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));

        assertThrows(RuntimeException.class, () -> pedidoService.comprar(1L, 10L, 1));
    }
}
