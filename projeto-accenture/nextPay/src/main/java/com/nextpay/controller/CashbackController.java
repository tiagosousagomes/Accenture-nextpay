package com.nextpay.controller;

import com.nextpay.dto.ResgateCashbackRequest;
import com.nextpay.dto.ResgateCashbackResponse;
import com.nextpay.dto.SaldoCashbackResponse;
import com.nextpay.service.CashbackService;
import com.nextpay.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cashback")
public class CashbackController {

    private final CashbackService cashbackService;
    private final ClienteService clienteService;

    public CashbackController(CashbackService cashbackService, ClienteService clienteService) {
        this.cashbackService = cashbackService;
        this.clienteService = clienteService;
    }

    @PostMapping("/resgatar")
    public ResponseEntity<ResgateCashbackResponse> resgatar(@RequestBody @Valid ResgateCashbackRequest req) {
        var r = cashbackService.resgatar(req.clienteId(), req.pontos());
        return ResponseEntity.ok(ResgateCashbackResponse.from(r));
    }

    @GetMapping("/saldo/{clienteId}")
    public ResponseEntity<SaldoCashbackResponse> saldo(@PathVariable UUID clienteId) {
        var cliente = clienteService.buscarEntidade(clienteId);
        var resp = new SaldoCashbackResponse(
                cliente.getId(),
                cliente.getNivel(),
                cliente.getPontosAcumulados(),
                cliente.getSaldoCashback(),
                cashbackService.pontosParaProximoNivel(cliente.getPontosAcumulados()),
                cashbackService.proximoNivel(cliente.getNivel())
        );
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/liberar-pendentes")
    public ResponseEntity<Integer> liberarPendentes() {
        return ResponseEntity.ok(cashbackService.liberarCashbacksPendentes());
    }
}
