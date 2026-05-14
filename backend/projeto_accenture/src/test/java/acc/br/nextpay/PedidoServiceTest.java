package acc.br.nextpay;

import acc.br.nextpay.dto.PedidoDTO;
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

    @Test
    void testComprarComSucesso() {

        Usuario comprador = new Usuario();
        comprador.setId(1L);
        comprador.setNome("Comprador");
        comprador.setConta(ContaCorrente.builder().saldo(BigDecimal.valueOf(1000)).build());

        Usuario vendedor = new Usuario();
        vendedor.setId(2L);
        vendedor.setNome("Vendedor");
        vendedor.setConta(ContaCorrente.builder().saldo(BigDecimal.ZERO).build());

        Produto produto = new Produto();
        produto.setId(10L);
        produto.setNome("Teclado");
        produto.setPreco(BigDecimal.valueOf(100));
        produto.setQuantidadeEstoque(10);
        produto.setVendedor(vendedor);

        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.of(comprador));
        Mockito.when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));

        Pedido pedidoFake = Pedido.builder()
                .id(1L).comprador(comprador).vendedor(vendedor).produto(produto)
                .quantidade(1).valorTotal(BigDecimal.valueOf(100)).status(StatusPedido.FINALIZADO)
                .build();
        Mockito.when(pedidoRepository.save(any())).thenReturn(pedidoFake);

        PedidoDTO result = pedidoService.comprar(1L, 10L, 1);

        assertNotNull(result);
        assertEquals("FINALIZADO", result.getStatus());
        Mockito.verify(transacaoService, Mockito.times(2)).registrarTransacao(any(), any(), any(), any());
    }

    @Test
    void testReservarEConfirmarReserva() {
        Usuario comprador = new Usuario(); comprador.setId(1L); comprador.setNome("C");
        comprador.setConta(ContaCorrente.builder().saldo(BigDecimal.valueOf(500)).build());

        Usuario vendedor = new Usuario(); vendedor.setId(2L); vendedor.setNome("V");
        vendedor.setConta(ContaCorrente.builder().saldo(BigDecimal.ZERO).build());

        Produto produto = new Produto();
        produto.setVendedor(vendedor);
        produto.setPreco(BigDecimal.valueOf(50));
        produto.setQuantidadeEstoque(10);

        Pedido pedidoReservado = Pedido.builder()
                .id(1L).comprador(comprador).vendedor(vendedor).produto(produto)
                .quantidade(1).valorTotal(BigDecimal.valueOf(50)).status(StatusPedido.RESERVADO)
                .build();

        Mockito.when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoReservado));
        Mockito.when(pedidoRepository.save(any())).thenReturn(pedidoReservado);

        PedidoDTO result = pedidoService.confirmarReserva(1L, comprador.getId());

        assertEquals("FINALIZADO", result.getStatus());
    }

    @Test
    void testErroSaldoInsuficiente() {
        Usuario comprador = new Usuario();
        comprador.setConta(ContaCorrente.builder().saldo(BigDecimal.ZERO).build());

        Produto produto = new Produto();
        produto.setPreco(BigDecimal.valueOf(100));
        produto.setQuantidadeEstoque(10);
        produto.setVendedor(new Usuario());

        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.of(comprador));
        Mockito.when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));

        assertThrows(RuntimeException.class, () -> pedidoService.comprar(1L, 10L, 1));
    }

    @Test
    void testErroComprarProprioProduto() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Produto produto = new Produto();
        produto.setVendedor(usuario);

        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        Mockito.when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));

        assertThrows(RuntimeException.class, () -> pedidoService.comprar(1L, 10L, 1));
    }
}