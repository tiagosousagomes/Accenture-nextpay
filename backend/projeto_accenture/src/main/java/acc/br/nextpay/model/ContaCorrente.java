package acc.br.nextpay.model;

import acc.br.nextpay.model.enums.StatusConta;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "contas_correntes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContaCorrente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String numeroConta;

    @NotNull
    @Builder.Default
    private BigDecimal saldo = BigDecimal.ZERO; // Default = 0 conforme rascunho

    @NotNull
    @Builder.Default
    private BigDecimal limite = new BigDecimal("300.00"); // Default = RS300

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusConta status = StatusConta.ATIVO; // Default = ATIVO

    @OneToOne
    @JoinColumn(name = "usuario_id")
    @JsonBackReference
    private Usuario usuario;

    /**
     * Regra de Negócio: Verifica se há saldo + limite disponível para uma transação.
     * @param valor O valor da compra
     * @return true se o cliente puder pagar
     */
    public boolean possuiSaldoSuficiente(BigDecimal valor) {
        if (this.status != StatusConta.ATIVO) return false;
        BigDecimal disponivelTotal = this.saldo.add(this.limite);
        return disponivelTotal.compareTo(valor) >= 0;
    }
}