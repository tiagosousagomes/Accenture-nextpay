package acc.br.nextpay.controller;

import acc.br.nextpay.model.Produto;
import acc.br.nextpay.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<Produto> cadastrar(@RequestBody Produto produto, @PathVariable Long usuarioId) {
        return new ResponseEntity<>(produtoService.cadastrarProduto(produto, usuarioId), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Produto>> listarTodos() {
        return ResponseEntity.ok(produtoService.listarProdutosComEstoque());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Produto>> listarProdutosDoUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(produtoService.listarProdutosDoUsuario(usuarioId));
    }

    @PutMapping("/{produtoId}/usuario/{usuarioId}")
    public ResponseEntity<Produto> editar(
            @PathVariable Long produtoId,
            @PathVariable Long usuarioId,
            @RequestBody Produto produto) {

        return ResponseEntity.ok(produtoService.editarProduto(produtoId, usuarioId, produto));
    }

    @DeleteMapping("/{produtoId}/usuario/{usuarioId}")
    public ResponseEntity<Map<String, String>> excluir(
            @PathVariable Long produtoId,
            @PathVariable Long usuarioId) {

        produtoService.excluirProduto(produtoId, usuarioId);
        return ResponseEntity.ok(Map.of("mensagem", "Produto excluído com sucesso."));
    }
}