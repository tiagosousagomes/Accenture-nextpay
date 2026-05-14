package acc.br.nextpay.controller;

import acc.br.nextpay.ai.AssistenteRegrasNegocio;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ajuda")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AjudaController {

    private final AssistenteRegrasNegocio assistenteRegrasNegocio;

    @PostMapping("/perguntar")
    public ResponseEntity<Map<String, String>> perguntar(@RequestBody Map<String, String> request) {
        String pergunta = request.get("pergunta");

        String resposta = assistenteRegrasNegocio.perguntar(pergunta);
        return ResponseEntity.ok(Map.of("resposta", resposta));
    }
}