package com.nextpay.controller;

import com.nextpay.dto.ClienteRequest;
import com.nextpay.dto.ClienteResponse;
import com.nextpay.dto.ContaResponse;
import com.nextpay.dto.TransacaoResponse;
import com.nextpay.repository.ContaRepository;
import com.nextpay.service.ClienteService;
import com.nextpay.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final TransacaoService transacaoService;
    private final ContaRepository contaRepository;

    public ClienteController(ClienteService clienteService,
                             TransacaoService transacaoService,
                             ContaRepository contaRepository) {
        this.clienteService = clienteService;
        this.transacaoService = transacaoService;
        this.contaRepository = contaRepository;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@RequestBody @Valid ClienteRequest req) {
        var resp = clienteService.criar(req);
        return ResponseEntity.created(URI.create("/api/clientes/" + resp.id())).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(clienteService.buscar(id));
    }

    @GetMapping("/{id}/extrato")
    public ResponseEntity<List<TransacaoResponse>> extrato(@PathVariable UUID id) {
        var lista = transacaoService.extratoPorCliente(id).stream()
                .map(TransacaoResponse::from)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}/contas")
    public ResponseEntity<List<ContaResponse>> contas(@PathVariable UUID id) {
        clienteService.buscar(id);
        var lista = contaRepository.findByClienteId(id).stream()
                .map(ContaResponse::from)
                .toList();
        return ResponseEntity.ok(lista);
    }
}
