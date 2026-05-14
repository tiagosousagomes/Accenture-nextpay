package acc.br.nextpay.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do produto é obrigatório")
    private String nome;

    private String descricao;

    @NotNull(message = "O preço é obrigatório")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    private BigDecimal preco;

    @NotNull(message = "A quantidade em estoque é obrigatória")
    @Min(value = 0, message = "O estoque não pode ser negativo")
    private Integer quantidadeEstoque;

    @ManyToOne
    @JoinColumn(name = "vendedor_id")
    @JsonIgnoreProperties({"produtos", "senha", "conta", "endereco"})
    private Usuario vendedor;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fotoProduto;

    @Builder.Default
    private boolean ativo = true;

    public boolean temEstoqueDisponivel(int quantidade) {
        return quantidade > 0 && this.quantidadeEstoque >= quantidade;
    }

    public void baixarEstoque(int quantidade) {
        if (!temEstoqueDisponivel(quantidade)) {
            throw new RuntimeException("Estoque insuficiente para este produto.");
        }
        this.quantidadeEstoque -= quantidade;
    }

    public void devolverEstoque(int quantidade) {
        this.quantidadeEstoque += quantidade;
    }
}