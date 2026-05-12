package com.nextpay.controller;

import com.nextpay.dto.TransacaoRequest;
import com.nextpay.dto.TransacaoResponse;
import com.nextpay.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @PostMapping
    public ResponseEntity<TransacaoResponse> registrar(@RequestBody @Valid TransacaoRequest req) {
        var t = transacaoService.registrar(req);
        var resp = TransacaoResponse.from(t);
        return ResponseEntity.created(URI.create("/api/transacoes/" + resp.id())).body(resp);
    }
}
