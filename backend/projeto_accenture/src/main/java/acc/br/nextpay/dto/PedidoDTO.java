package acc.br.nextpay.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PedidoDTO {

    private Long id;
    private String nomeComprador;
    private String nomeVendedor;
    private String nomeProduto;
    private Integer quantidade;
    private BigDecimal valorTotal;
    private LocalDateTime dataPedido;
    private String status;
}