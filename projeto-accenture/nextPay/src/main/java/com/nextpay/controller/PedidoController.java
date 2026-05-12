package com.nextpay.controller;

import com.nextpay.dto.PagamentoRequest;
import com.nextpay.dto.PagamentoResponse;
import com.nextpay.dto.PedidoRequest;
import com.nextpay.dto.PedidoResponse;
import com.nextpay.service.PagamentoService;
import com.nextpay.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final PagamentoService pagamentoService;

    public PedidoController(PedidoService pedidoService, PagamentoService pagamentoService) {
        this.pedidoService = pedidoService;
        this.pagamentoService = pagamentoService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> criar(@RequestBody @Valid PedidoRequest req) {
        var pedido = pedidoService.criar(req);
        var resp = PedidoResponse.from(pedido);
        return ResponseEntity.created(URI.create("/api/pedidos/" + resp.id())).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(PedidoResponse.from(pedidoService.buscar(id)));
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<PagamentoResponse> pagar(@PathVariable UUID id,
                                                   @RequestBody @Valid PagamentoRequest req) {
        var pagamento = pagamentoService.pagar(id, req);
        var resp = PagamentoResponse.from(pagamento);
        return ResponseEntity.created(URI.create("/api/pedidos/" + id + "/pagamento")).body(resp);
    }
}
