package acc.br.nextpay.model;

import acc.br.nextpay.model.enums.NivelUsuario;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    private boolean emailConfirmado = false;
    private String tokenConfirmacao;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "O CPF/CNPJ é obrigatório")
    @Column(unique = true)
    private String cpfCnpj;

    @Email(message = "E-mail inválido")
    @NotBlank(message = "O e-mail é obrigatório")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    private String senha;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private ContaCorrente conta;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "endereco_id")
    @JsonManagedReference // Added this annotation
    private Endereco endereco;

    @OneToMany(mappedBy = "vendedor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("vendedor")
    private List<Produto> produtos;

    @Builder.Default
    private Integer pontuacao = 0;

    @Builder.Default
    private Integer totalTransacoes = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NivelUsuario nivel = NivelUsuario.BRONZE;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fotoPerfil;

    public void adicionarPontuacao(int pontos) {
        this.pontuacao += pontos;
        atualizarNivel();
    }

    public void adicionarTransacao() {
        this.totalTransacoes++;
        atualizarNivel();
    }

    private void atualizarNivel() {
        this.nivel = NivelUsuario.fromTransacoes(this.totalTransacoes);
    }

    public String getFotoPerfilUrl() {
        if (fotoPerfil == null || fotoPerfil.isBlank()) {
            return "";
        }
        return "data:image/jpeg;base64," + fotoPerfil;
    }

    public void setFotoPerfilUrl(String u) {
    }

    public NivelUsuario getNivel() {
        return this.nivel;
    }

    public void setNivel(NivelUsuario nivel) {
        this.nivel = nivel;
    }

    public boolean isEmailConfirmado() {
        return emailConfirmado;
    }

    public void setEmailConfirmado(boolean emailConfirmado) {
        this.emailConfirmado = emailConfirmado;
    }

    public String getTokenConfirmacao() {
        return tokenConfirmacao;
    }

    public void setTokenConfirmacao(String tokenConfirmacao) {
        this.tokenConfirmacao = tokenConfirmacao;
    }

}