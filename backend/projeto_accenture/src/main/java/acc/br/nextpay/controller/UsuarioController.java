package acc.br.nextpay.controller;

import acc.br.nextpay.dto.UsuarioResponse;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(
            @RequestBody @Valid Usuario usuario,
            @RequestParam String cep,
            @RequestParam String numero) {

        Usuario novoUsuario = usuarioService.cadastrarUsuario(usuario, cep, numero);
        return new ResponseEntity<>(UsuarioResponse.from(novoUsuario), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        List<UsuarioResponse> lista = usuarioService.listarTodos()
                .stream()
                .map(UsuarioResponse::from)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(UsuarioResponse.from(usuarioService.buscarPorId(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> excluirConta(@PathVariable Long id) {
        usuarioService.excluirConta(id, getUsuarioIdAutenticado());
        return ResponseEntity.ok(Map.of("mensagem", "Conta excluída com sucesso."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizarUsuario(
            @PathVariable Long id,
            @RequestBody Usuario usuario) {

        Usuario atualizado = usuarioService.atualizarUsuario(id, getUsuarioIdAutenticado(), usuario);
        return ResponseEntity.ok(UsuarioResponse.from(atualizado));
    }

    private Long getUsuarioIdAutenticado() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
