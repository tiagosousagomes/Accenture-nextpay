package com.nextpay.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "pagamento",
        uniqueConstraints = @UniqueConstraint(columnNames = "pedido_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_origem_id", nullable = false)
    private Conta contaOrigem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPagamento metodo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusPagamento status = StatusPagamento.PENDENTE;

    @Column(name = "pago_em")
    private LocalDateTime pagoEm;

    @OneToMany(mappedBy = "pagamento", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Transacao> transacoes = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (status == null) status = StatusPagamento.PENDENTE;
    }

    public enum MetodoPagamento { PIX, BOLETO, SALDO }
    public enum StatusPagamento { PENDENTE, CONCLUIDO, CANCELADO, ESTORNADO }
}
