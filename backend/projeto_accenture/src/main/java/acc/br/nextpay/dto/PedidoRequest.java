package acc.br.nextpay.dto;

import lombok.Data;

@Data
public class PedidoRequest {

    private Long compradorId;
    private Long produtoId;
    private Integer quantidade;
}