package com.nextpay.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "pedido")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprador_id", nullable = false)
    private Cliente comprador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "endereco_entrega_id", nullable = false)
    private Endereco enderecoEntrega;

    @Column(name = "valor_total", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusPedido status = StatusPedido.CRIADO;

    @Column(name = "cashback_gerado", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal cashbackGerado = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime data;

    @OneToMany(mappedBy = "pedido", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemPedido> itens = new ArrayList<>();

    @OneToOne(mappedBy = "pedido", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Pagamento pagamento;

    @PrePersist
    void prePersist() {
        if (data == null) data = LocalDateTime.now();
        if (status == null) status = StatusPedido.CRIADO;
        if (valorTotal == null) valorTotal = BigDecimal.ZERO;
        if (cashbackGerado == null) cashbackGerado = BigDecimal.ZERO;
    }

    public enum StatusPedido { CRIADO, PAGO, ENVIADO, ENTREGUE, CANCELADO }
}
