package acc.br.nextpay.controller;

import acc.br.nextpay.model.Transacao;
import acc.br.nextpay.service.TransacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacoes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransacaoController {

    private final TransacaoService transacaoService;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Transacao>> getHistorico(@PathVariable Long usuarioId) {
        List<Transacao> transacoes = transacaoService.listarTransacoesPorUsuario(usuarioId);
        return ResponseEntity.ok(transacoes);
    }
}
