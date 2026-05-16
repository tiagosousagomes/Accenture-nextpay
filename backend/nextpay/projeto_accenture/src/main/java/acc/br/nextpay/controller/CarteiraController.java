package acc.br.nextpay.controller;

import acc.br.nextpay.dto.CarteiraRequest;
import acc.br.nextpay.model.ContaCorrente;
import acc.br.nextpay.repository.ContaCorrenteRepository;
import acc.br.nextpay.service.TransacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/carteiras")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CarteiraController {

    private final ContaCorrenteRepository contaCorrenteRepository;
    private final TransacaoService transacaoService;

    @GetMapping("/saldos")
    public List<ContaCorrente> listarSaldos() {
        return contaCorrenteRepository.findAll();
    }

    @PostMapping("/deposito")
    public ResponseEntity<ContaCorrente> depositar(@RequestBody CarteiraRequest request) {
        return ResponseEntity.ok(transacaoService.depositar(request.getUsuarioId(), request.getValor()));
    }

    @PostMapping("/saque")
    public ResponseEntity<ContaCorrente> sacar(@RequestBody CarteiraRequest request) {
        return ResponseEntity.ok(transacaoService.sacar(request.getUsuarioId(), request.getValor()));
    }

    @PostMapping("/pix")
        public ResponseEntity<Map<String, String>> pix(@RequestBody CarteiraRequest request) {
        String mensagem = transacaoService.pix(
                request.getUsuarioOrigemId(),
                request.getChavePix(),
                request.getValor()
        );

        return ResponseEntity.ok(Map.of("mensagem", mensagem));
    }

    @PostMapping("/transferencia")
    public ResponseEntity<Map<String, String>> transferir(@RequestBody CarteiraRequest request) {
        String mensagem = transacaoService.transferir(
                request.getUsuarioOrigemId(),
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
}