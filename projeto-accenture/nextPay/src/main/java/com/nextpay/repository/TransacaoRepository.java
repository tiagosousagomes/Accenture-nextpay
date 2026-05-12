package com.nextpay.repository;

import com.nextpay.entity.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, UUID> {
    List<Transacao> findByContaIdOrderByDataDesc(UUID contaId);
    List<Transacao> findByStatusAndDataBefore(Transacao.StatusTransacao status, LocalDateTime data);
    List<Transacao> findByContaClienteIdOrderByDataDesc(UUID clienteId);
}
