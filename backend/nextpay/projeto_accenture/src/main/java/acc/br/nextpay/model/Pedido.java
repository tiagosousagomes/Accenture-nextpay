package acc.br.nextpay.model;

import acc.br.nextpay.model.enums.StatusPedido;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "comprador_id")
    @JsonIgnoreProperties({"conta", "endereco", "produtos"})
    private Usuario comprador;

    @ManyToOne
    @JoinColumn(name = "vendedor_id")
    @JsonIgnoreProperties({"conta", "endereco", "produtos"})
    private Usuario vendedor;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    @JsonIgnoreProperties("vendedor")
    private Produto produto;

    @NotNull
    private Integer quantidade;

    @NotNull
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusPedido status = StatusPedido.RESERVADO;

    @Builder.Default
    private LocalDateTime dataPedido = LocalDateTime.now();
}