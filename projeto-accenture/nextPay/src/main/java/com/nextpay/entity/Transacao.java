package com.nextpay.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transacao")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_id", nullable = false)
    private Conta conta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pagamento_id")
    private Pagamento pagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTransacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaTransacao categoria;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusTransacao status = StatusTransacao.PENDENTE;

    @Column(name = "valor_cashback_gerado", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal valorCashbackGerado = BigDecimal.ZERO;

    private String descricao;

    @Column(nullable = false)
    private LocalDateTime data;

    @PrePersist
    void prePersist() {
        if (data == null) data = LocalDateTime.now();
        if (status == null) status = StatusTransacao.PENDENTE;
        if (valorCashbackGerado == null) valorCashbackGerado = BigDecimal.ZERO;
    }

    public enum TipoTransacao { DEPOSITO, PIX, COMPRA, ESTORNO, CREDITO_CASHBACK }
    public enum CategoriaTransacao { ALIMENTACAO, TRANSPORTE, MARKETPLACE, LAZER, SERVICOS, OUTROS }
    public enum StatusTransacao { PENDENTE, CONCLUIDA, CANCELADA, ESTORNADA }
}
