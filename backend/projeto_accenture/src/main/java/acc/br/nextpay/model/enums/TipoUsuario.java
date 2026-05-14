package acc.br.nextpay.model.enums;

import lombok.Getter;

/**
 * Define os perfis de acesso no sistema de Marketplace.
 * Conforme rascunho: Default é COMPRADOR.
 */
@Getter
public enum TipoUsuario {
    COMPRADOR("Comprador"),
    VENDEDOR("Vendedor"),
    AMBOS("Ambos");

    private final String descricao;

    TipoUsuario(String descricao) {
        this.descricao = descricao;
    }
}