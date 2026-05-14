package acc.br.nextpay.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Table(name = "enderecos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O CEP é obrigatório")
    @Pattern(regexp = "\\d{8}", message = "O CEP deve conter 8 dígitos numéricos")
    private String cep;

    @NotBlank(message = "O logradouro é obrigatório")
    private String logradouro;

    private String complemento;

    @NotBlank(message = "O bairro é obrigatório")
    private String bairro;

    @NotBlank(message = "A cidade é obrigatória")
    private String localidade;

    @NotBlank(message = "A UF é obrigatória")
    private String uf;

    @NotBlank(message = "O número é obrigatório")
    private String numero;

    @OneToOne(mappedBy = "endereco")
    @JsonBackReference
    private Usuario usuario;
}