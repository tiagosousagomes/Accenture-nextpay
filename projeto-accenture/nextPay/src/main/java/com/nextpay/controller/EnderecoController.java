package com.nextpay.controller;

import com.nextpay.dto.EnderecoRequest;
import com.nextpay.dto.EnderecoResponse;
import com.nextpay.service.EnderecoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
public class EnderecoController {

    private final EnderecoService enderecoService;

    public EnderecoController(EnderecoService enderecoService) {
        this.enderecoService = enderecoService;
    }

    @PostMapping("/api/clientes/{clienteId}/enderecos")
    public ResponseEntity<EnderecoResponse> criar(@PathVariable UUID clienteId,
                                                  @RequestBody @Valid EnderecoRequest req) {
        var endereco = enderecoService.criar(clienteId, req);
        var resp = EnderecoResponse.from(endereco);
        return ResponseEntity.created(URI.create("/api/enderecos/" + resp.id())).body(resp);
    }

    @GetMapping("/api/clientes/{clienteId}/enderecos")
    public ResponseEntity<List<EnderecoResponse>> listarPorCliente(@PathVariable UUID clienteId) {
        var lista = enderecoService.listarPorCliente(clienteId).stream()
                .map(EnderecoResponse::from)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/api/enderecos/{id}")
    public ResponseEntity<EnderecoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(EnderecoResponse.from(enderecoService.buscar(id)));
    }

    @PutMapping("/api/enderecos/{id}")
    public ResponseEntity<EnderecoResponse> atualizar(@PathVariable UUID id,
                                                      @RequestBody @Valid EnderecoRequest req) {
        return ResponseEntity.ok(EnderecoResponse.from(enderecoService.atualizar(id, req)));
    }

    @DeleteMapping("/api/enderecos/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        enderecoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
