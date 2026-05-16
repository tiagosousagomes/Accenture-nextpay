package acc.br.nextpay.controller;

import acc.br.nextpay.dto.LoginRequest;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Usuario usuario = usuarioService.login(request.getEmail(), request.getSenha());

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("mensagem", "Login realizado com sucesso.");
        resposta.put("usuarioId", usuario.getId());
        resposta.put("nome", usuario.getNome());
        resposta.put("email", usuario.getEmail());

        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("mensagem", "Logout realizado com sucesso."));
    }
}
