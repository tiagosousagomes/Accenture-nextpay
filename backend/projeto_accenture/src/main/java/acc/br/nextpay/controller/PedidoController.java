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
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping("/comprar")
    public ResponseEntity<PedidoDTO> comprar(@RequestBody PedidoRequest request) {
        return ResponseEntity.ok(
                pedidoService.comprar(getUsuarioIdAutenticado(), request.getProdutoId(), request.getQuantidade())
        );
    }

    @PostMapping("/reservar")
    public ResponseEntity<PedidoDTO> reservar(@RequestBody PedidoRequest request) {
        return ResponseEntity.ok(
                pedidoService.reservar(getUsuarioIdAutenticado(), request.getProdutoId(), request.getQuantidade())
        );
    }

    @PutMapping("/{pedidoId}/confirmar")
    public ResponseEntity<PedidoDTO> confirmarReserva(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(pedidoService.confirmarReserva(pedidoId, getUsuarioIdAutenticado()));
    }

    @PutMapping("/{pedidoId}/cancelar")
    public ResponseEntity<PedidoDTO> cancelarReserva(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(pedidoService.cancelarReserva(pedidoId, getUsuarioIdAutenticado()));
    }

    @GetMapping
    public ResponseEntity<List<Pedido>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    private Long getUsuarioIdAutenticado() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
