package acc.br.nextpay.controller;

import acc.br.nextpay.dto.CarteiraRequest;
import acc.br.nextpay.model.ContaCorrente;
import acc.br.nextpay.service.TransacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/carteiras")
@RequiredArgsConstructor
public class CarteiraController {

    private final TransacaoService transacaoService;

    @GetMapping("/saldo")
    public ResponseEntity<ContaCorrente> saldo() {
        return ResponseEntity.ok(transacaoService.buscarContaPorUsuario(getUsuarioIdAutenticado()));
    }

    @PostMapping("/deposito")
    public ResponseEntity<ContaCorrente> depositar(@RequestBody CarteiraRequest request) {
        return ResponseEntity.ok(transacaoService.depositar(getUsuarioIdAutenticado(), request.getValor()));
    }

    @PostMapping("/saque")
    public ResponseEntity<ContaCorrente> sacar(@RequestBody CarteiraRequest request) {
        return ResponseEntity.ok(transacaoService.sacar(getUsuarioIdAutenticado(), request.getValor()));
    }

    @PostMapping("/pix")
    public ResponseEntity<Map<String, String>> pix(@RequestBody CarteiraRequest request) {
        String mensagem = transacaoService.pix(
                getUsuarioIdAutenticado(),
                request.getChavePix(),
                request.getValor()
        );
        return ResponseEntity.ok(Map.of("mensagem", mensagem));
    }

    @PostMapping("/transferencia")
    public ResponseEntity<Map<String, String>> transferir(@RequestBody CarteiraRequest request) {
        String mensagem = transacaoService.transferir(
                getUsuarioIdAutenticado(),
                request.getUsuarioDestinoId(),
                request.getValor()
        );
        return ResponseEntity.ok(Map.of("mensagem", mensagem));
    }

    @GetMapping("/pix/buscar")
    public ResponseEntity<Map<String, String>> buscarChavePix(@RequestParam String chave) {
        var usuario = transacaoService.buscarUsuarioPorChavePix(chave);
        return ResponseEntity.ok(Map.of(
                "id", usuario.getId().toString(),
                "nome", usuario.getNome(),
                "email", usuario.getEmail(),
                "cpfCnpj", usuario.getCpfCnpj()
        ));
    }

    private Long getUsuarioIdAutenticado() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
