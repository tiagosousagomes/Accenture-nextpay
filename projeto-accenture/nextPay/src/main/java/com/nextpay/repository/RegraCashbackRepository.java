package com.nextpay.repository;

import com.nextpay.entity.Cliente;
import com.nextpay.entity.RegraCashback;
import com.nextpay.entity.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegraCashbackRepository extends JpaRepository<RegraCashback, UUID> {

    Optional<RegraCashback> findByCategoriaAndNivelMinimoAndAtivoTrue(
            Transacao.CategoriaTransacao categoria, Cliente.NivelCliente nivelMinimo);

    @Query("""
           SELECT r FROM RegraCashback r
           WHERE r.categoria = :categoria
             AND r.ativo = true
             AND r.nivelMinimo IN :niveis
           ORDER BY r.percentual DESC
           """)
    Optional<RegraCashback> findMelhorRegra(@Param("categoria") Transacao.CategoriaTransacao categoria,
                                            @Param("niveis") java.util.List<Cliente.NivelCliente> niveis);
}
