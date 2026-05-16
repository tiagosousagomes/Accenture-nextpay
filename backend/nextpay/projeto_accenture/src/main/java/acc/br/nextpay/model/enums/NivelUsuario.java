package acc.br.nextpay.model.enums;

import lombok.Getter;

@Getter
public enum NivelUsuario {
    BRONZE("Bronze", 0, 0.01),
    PRATA("Prata", 5, 0.02),
    OURO("Ouro", 10, 0.03),
    DIAMANTE("Diamante", 20, 0.05);

    private final String descricao;
    private final int transacoesMinimas;
    private final double percentualCashback;

    NivelUsuario(String descricao, int transacoesMinimas, double percentualCashback) {
        this.descricao = descricao;
        this.transacoesMinimas = transacoesMinimas;
        this.percentualCashback = percentualCashback;
    }

    public static NivelUsuario fromTransacoes(int totalTransacoes) {
        if (totalTransacoes >= DIAMANTE.transacoesMinimas) {
            return DIAMANTE;
        } else if (totalTransacoes >= OURO.transacoesMinimas) {
            return OURO;
        } else if (totalTransacoes >= PRATA.transacoesMinimas) {
            return PRATA;
        } else {
            return BRONZE;
        }
    }
}
