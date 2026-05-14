package acc.br.nextpay.service;

import acc.br.nextpay.exception.NegocioException;
import acc.br.nextpay.exception.RecursoNaoEncontradoException;
import acc.br.nextpay.model.Produto;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.repository.ProdutoRepository;
import acc.br.nextpay.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;

    public Produto cadastrarProduto(Produto produto, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        produto.setVendedor(usuario);

        return produtoRepository.save(produto);
    }

    public List<Produto> listarProdutosComEstoque() {
        return produtoRepository.findByAtivoTrueAndQuantidadeEstoqueGreaterThan(0);
    }

    public List<Produto> listarProdutosDoUsuario(Long usuarioId) {
        return produtoRepository.findByVendedorId(usuarioId);
    }

    @Transactional
    public Produto editarProduto(Long produtoId, Long usuarioId, Produto dadosAtualizados) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));

        validarDonoProduto(produto, usuarioId);

        produto.setNome(dadosAtualizados.getNome());
        produto.setDescricao(dadosAtualizados.getDescricao());
        produto.setPreco(dadosAtualizados.getPreco());
        produto.setQuantidadeEstoque(dadosAtualizados.getQuantidadeEstoque());
        produto.setFotoProduto(dadosAtualizados.getFotoProduto());

        return produtoRepository.saveAndFlush(produto);
    }

    @Transactional
    public void excluirProduto(Long produtoId, Long usuarioId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));

        validarDonoProduto(produto, usuarioId);

        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    private void validarDonoProduto(Produto produto, Long usuarioId) {
        if (!produto.getVendedor().getId().equals(usuarioId)) {
            throw new NegocioException("Apenas o dono do produto pode editar ou excluir este produto.");
        }
    }
}
