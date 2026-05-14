package acc.br.nextpay.dto;

import acc.br.nextpay.model.ContaCorrente;
import acc.br.nextpay.model.Endereco;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.model.enums.NivelUsuario;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsuarioResponse {

    private Long id;
    private String nome;
    private String email;
    private String cpfCnpj;
    private Integer pontuacao;
    private NivelUsuario nivel;
    private String fotoPerfilUrl;
    private ContaCorrente conta;
    private Endereco endereco;

    public static UsuarioResponse from(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .cpfCnpj(usuario.getCpfCnpj())
                .pontuacao(usuario.getPontuacao())
                .nivel(usuario.getNivel())
                .fotoPerfilUrl(usuario.getFotoPerfilUrl())
                .conta(usuario.getConta())
                .endereco(usuario.getEndereco())
                .build();
    }
}
