package acc.br.nextpay.model.enums;

import lombok.Getter;

@Getter
public enum NivelUsuario {
    BRONZE("Bronze", 0),
    PRATA("Prata", 5),
    OURO("Ouro", 10);

    private final String descricao;
    private final int pontuacaoMinima;

    NivelUsuario(String descricao, int pontuacaoMinima) {
        this.descricao = descricao;
        this.pontuacaoMinima = pontuacaoMinima;
    }

    public static NivelUsuario fromPontuacao(int pontuacao) {
        if (pontuacao >= OURO.pontuacaoMinima) {
            return OURO;
        } else if (pontuacao >= PRATA.pontuacaoMinima) {
            return PRATA;
        } else {
            return BRONZE;
        }
    }
}
