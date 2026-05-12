package com.nextpay.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "resgate_cashback")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResgateCashback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TipoResgate tipo = TipoResgate.CREDITO_CONTA;

    @Column(name = "pontos_convertidos", nullable = false)
    private Long pontosConvertidos;

    @Column(name = "valor_real", nullable = false, precision = 19, scale = 2)
    private BigDecimal valorReal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusResgate status = StatusResgate.PENDENTE;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void prePersist() {
        if (criadoEm == null) criadoEm = LocalDateTime.now();
        if (status == null) status = StatusResgate.PENDENTE;
        if (tipo == null) tipo = TipoResgate.CREDITO_CONTA;
    }

    public enum TipoResgate { CREDITO_CONTA }
    public enum StatusResgate { PENDENTE, CONCLUIDO, CANCELADO }
}
