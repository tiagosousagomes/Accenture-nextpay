package com.nextpay.repository;

import com.nextpay.entity.HistoricoNivel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface HistoricoNivelRepository extends JpaRepository<HistoricoNivel, UUID> {
    List<HistoricoNivel> findByClienteIdOrderByDataAlteracaoDesc(UUID clienteId);
}
