package acc.br.nextpay.controller;

import acc.br.nextpay.model.Produto;
import acc.br.nextpay.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping
    public ResponseEntity<Produto> cadastrar(@RequestBody Produto produto) {
        return new ResponseEntity<>(produtoService.cadastrarProduto(produto, getUsuarioIdAutenticado()), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Produto>> listarTodos() {
        return ResponseEntity.ok(produtoService.listarProdutosComEstoque());
    }

    @GetMapping("/meus")
    public ResponseEntity<List<Produto>> listarMeusProdutos() {
        return ResponseEntity.ok(produtoService.listarProdutosDoUsuario(getUsuarioIdAutenticado()));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Produto>> listarProdutosDoUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(produtoService.listarProdutosDoUsuario(usuarioId));
    }

    @PutMapping("/{produtoId}")
    public ResponseEntity<Produto> editar(
            @PathVariable Long produtoId,
            @RequestBody Produto produto) {

        return ResponseEntity.ok(produtoService.editarProduto(produtoId, getUsuarioIdAutenticado(), produto));
    }

    @DeleteMapping("/{produtoId}")
    public ResponseEntity<Map<String, String>> excluir(@PathVariable Long produtoId) {
        produtoService.excluirProduto(produtoId, getUsuarioIdAutenticado());
        return ResponseEntity.ok(Map.of("mensagem", "Produto desativado com sucesso."));
    }

    private Long getUsuarioIdAutenticado() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
