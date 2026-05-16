package acc.br.nextpay;

import acc.br.nextpay.model.Produto;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.repository.ProdutoRepository;
import acc.br.nextpay.repository.UsuarioRepository;
import acc.br.nextpay.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @InjectMocks
    private ProdutoService produtoService;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    void testCadastrarEListarProdutos() {
        Usuario vendedor = new Usuario();
        vendedor.setId(1L);
        Produto p = new Produto();

        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.of(vendedor));
        Mockito.when(produtoRepository.save(any())).thenReturn(p);
        Mockito.when(produtoRepository.findByQuantidadeEstoqueGreaterThan(0))
                .thenReturn(Collections.singletonList(p));

        assertNotNull(produtoService.cadastrarProduto(p, 1L));

        assertFalse(produtoService.listarProdutosComEstoque().isEmpty());
    }

    @Test
    void testEditarEValidarDono() {
        Usuario dono = new Usuario();
        dono.setId(1L);

        Produto p = new Produto();
        p.setId(10L);
        p.setVendedor(dono);

        Mockito.when(produtoRepository.findById(10L)).thenReturn(Optional.of(p));
        Mockito.when(produtoRepository.saveAndFlush(any())).thenReturn(p);

        Produto dados = new Produto();
        dados.setNome("Novo Nome");
        assertDoesNotThrow(() -> produtoService.editarProduto(10L, 1L, dados));

        assertThrows(RuntimeException.class, () -> {
            produtoService.editarProduto(10L, 99L, dados);
        });
    }

    @Test
    void testExcluirProduto() {
        Usuario dono = new Usuario();
        dono.setId(1L);
        Produto p = new Produto();
        p.setVendedor(dono);

        Mockito.when(produtoRepository.findById(10L)).thenReturn(Optional.of(p));

        assertDoesNotThrow(() -> produtoService.excluirProduto(10L, 1L));

        Mockito.verify(produtoRepository).save(p);
        assertEquals(0, p.getQuantidadeEstoque());
    }
}