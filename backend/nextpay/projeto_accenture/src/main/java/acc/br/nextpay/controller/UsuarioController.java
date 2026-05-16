package acc.br.nextpay.controller;

import acc.br.nextpay.dto.ConfirmacaoEmailDTO;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<Usuario> cadastrar(
            @RequestBody @Valid Usuario usuario,
            @RequestParam String cep,
            @RequestParam String numero) {

        Usuario novoUsuario = usuarioService.cadastrarUsuario(usuario, cep, numero);
        return new ResponseEntity<>(novoUsuario, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> excluirConta(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean confirmar) {

        usuarioService.excluirConta(id, confirmar);
        return ResponseEntity.ok(Map.of("mensagem", "Conta excluída com sucesso."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizarUsuario(
            @PathVariable Long id,
            @RequestBody Usuario usuario) {

        return ResponseEntity.ok(usuarioService.atualizarUsuario(id, usuario));
    }

    @PostMapping("/confirmar-email")
    public ResponseEntity<String> confirmarEmail(@RequestBody ConfirmacaoEmailDTO dto) {
        usuarioService.confirmarCodigoEmail(dto.getEmail(), dto.getCodigo());
        return ResponseEntity.ok("Cadastro realizado com sucesso.");
    }
}