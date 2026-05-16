package acc.br.nextpay.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarteiraRequest {

    private Long usuarioId;
    private Long usuarioOrigemId;
    private Long usuarioDestinoId;
    private BigDecimal valor;
    private String chavePix;
}