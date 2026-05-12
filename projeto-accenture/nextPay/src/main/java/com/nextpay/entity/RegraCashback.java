package com.nextpay.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "regra_cashback",
        uniqueConstraints = @UniqueConstraint(columnNames = {"categoria", "nivel_minimo"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegraCashback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Transacao.CategoriaTransacao categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_minimo", nullable = false)
    private Cliente.NivelCliente nivelMinimo;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentual;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @PrePersist
    void prePersist() {
        if (ativo == null) ativo = true;
    }
}
