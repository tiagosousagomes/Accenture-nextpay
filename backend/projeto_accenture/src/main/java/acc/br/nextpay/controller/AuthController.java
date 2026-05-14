package acc.br.nextpay.controller;

import acc.br.nextpay.dto.LoginRequest;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.security.JwtUtil;
import acc.br.nextpay.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Usuario usuario = usuarioService.login(request.getEmail(), request.getSenha());
        String token = jwtUtil.gerarToken(usuario.getId(), usuario.getEmail());

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("token", token);
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
