package acc.br.nextpay.controller;

import acc.br.nextpay.dto.PedidoDTO;
import acc.br.nextpay.dto.PedidoRequest;
import acc.br.nextpay.model.Pedido;
import acc.br.nextpay.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping("/comprar")
    public ResponseEntity<PedidoDTO> comprar(@RequestBody PedidoRequest request) {
        Long usuarioId = getUsuarioIdAutenticado();
        if (usuarioId == null) {
            usuarioId = request.getCompradorId();
        }
        return ResponseEntity.ok(
                pedidoService.comprar(usuarioId, request.getProdutoId(), request.getQuantidade(),
                        request.getMoedasUsadas()));
    }

    @PostMapping("/reservar")
    public ResponseEntity<PedidoDTO> reservar(@RequestBody PedidoRequest request) {
        return ResponseEntity.ok(
                pedidoService.reservar(request.getCompradorId(), request.getProdutoId(), request.getQuantidade()));
    }

    @PutMapping("/{pedidoId}/confirmar")
    public ResponseEntity<PedidoDTO> confirmarReserva(@PathVariable Long pedidoId,
            @RequestBody(required = false) PedidoRequest request) {
        java.math.BigDecimal moedas = (request != null) ? request.getMoedasUsadas() : null;
        Long usuarioId = getUsuarioIdAutenticado();
        if (usuarioId == null && request != null) {
            usuarioId = request.getCompradorId();
        }
        return ResponseEntity.ok(pedidoService.confirmarReserva(pedidoId, usuarioId, moedas));
    }

    @PutMapping("/{pedidoId}/cancelar")
    public ResponseEntity<PedidoDTO> cancelarReserva(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(pedidoService.cancelarReserva(pedidoId));
    }

    @GetMapping
    public ResponseEntity<List<Pedido>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    private Long getUsuarioIdAutenticado() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof Long) {
                return (Long) principal;
            }
            if (principal instanceof String) {
                String s = (String) principal;
                if (s.equals("anonymousUser")) {
                    return null;
                }
                return Long.parseLong(s);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}