package com.nextpay.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historico_nivel")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HistoricoNivel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false, updatable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_anterior", updatable = false)
    private Cliente.NivelCliente nivelAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_atual", nullable = false, updatable = false)
    private Cliente.NivelCliente nivelAtual;

    @Column(name = "pontos_no_momento", nullable = false, updatable = false)
    private Long pontosNoMomento;

    @Column(updatable = false)
    private String motivo;

    @Column(name = "data_alteracao", nullable = false, updatable = false)
    private LocalDateTime dataAlteracao;

    @PrePersist
    void prePersist() {
        if (dataAlteracao == null) dataAlteracao = LocalDateTime.now();
    }
}
