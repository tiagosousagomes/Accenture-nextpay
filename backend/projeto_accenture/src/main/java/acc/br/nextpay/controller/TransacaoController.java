package acc.br.nextpay.controller;

import acc.br.nextpay.model.Transacao;
import acc.br.nextpay.service.TransacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final TransacaoService transacaoService;

    @GetMapping
    public ResponseEntity<List<Transacao>> getHistorico() {
        return ResponseEntity.ok(transacaoService.listarTransacoesPorUsuario(getUsuarioIdAutenticado()));
    }

    private Long getUsuarioIdAutenticado() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
