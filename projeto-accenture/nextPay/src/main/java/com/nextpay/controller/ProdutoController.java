package com.nextpay.controller;

import com.nextpay.dto.ProdutoRequest;
import com.nextpay.dto.ProdutoResponse;
import com.nextpay.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping("/api/produtos")
    public ResponseEntity<ProdutoResponse> criar(@RequestBody @Valid ProdutoRequest req) {
        var produto = produtoService.criar(req);
        var resp = ProdutoResponse.from(produto);
        return ResponseEntity.created(URI.create("/api/produtos/" + resp.id())).body(resp);
    }

    @GetMapping("/api/produtos/{id}")
    public ResponseEntity<ProdutoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(ProdutoResponse.from(produtoService.buscar(id)));
    }

    @GetMapping("/api/clientes/{vendedorId}/produtos")
    public ResponseEntity<List<ProdutoResponse>> listarPorVendedor(@PathVariable UUID vendedorId) {
        var lista = produtoService.listarPorVendedor(vendedorId).stream()
                .map(ProdutoResponse::from)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/api/produtos")
    public ResponseEntity<List<ProdutoResponse>> listar(@RequestParam(required = false) String nome) {
        var lista = (nome != null && !nome.isBlank()
                ? produtoService.buscarPorNome(nome)
                : produtoService.listarTodos()).stream()
                .map(ProdutoResponse::from)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/api/produtos/{id}")
    public ResponseEntity<ProdutoResponse> atualizar(@PathVariable UUID id,
                                                     @RequestBody @Valid ProdutoRequest req) {
        return ResponseEntity.ok(ProdutoResponse.from(produtoService.atualizar(id, req)));
    }

    @DeleteMapping("/api/produtos/{id}")
    public ResponseEntity<Void> desativar(@PathVariable UUID id) {
        produtoService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
