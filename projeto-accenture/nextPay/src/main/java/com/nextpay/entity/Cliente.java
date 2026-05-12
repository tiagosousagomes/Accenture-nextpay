package com.nextpay.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "cliente",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "cpf"),
                @UniqueConstraint(columnNames = "email")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCliente tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NivelCliente nivel = NivelCliente.BRONZE;

    @Column(name = "pontos_acumulados", nullable = false)
    @Builder.Default
    private Long pontosAcumulados = 0L;

    @Column(name = "saldo_cashback", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal saldoCashback = BigDecimal.ZERO;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Endereco> enderecos = new ArrayList<>();

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Conta> contas = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (criadoEm == null) criadoEm = LocalDateTime.now();
        if (nivel == null) nivel = NivelCliente.BRONZE;
        if (pontosAcumulados == null) pontosAcumulados = 0L;
        if (saldoCashback == null) saldoCashback = BigDecimal.ZERO;
    }

    public enum TipoCliente { COMPRADOR, VENDEDOR, AMBOS }
    public enum NivelCliente { BRONZE, PRATA, OURO, PLATINA }
}
