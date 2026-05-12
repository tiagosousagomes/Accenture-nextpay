package com.nextpay.repository;

import com.nextpay.entity.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContaRepository extends JpaRepository<Conta, UUID> {
    List<Conta> findByClienteIdAndStatus(UUID clienteId, Conta.StatusConta status);
    List<Conta> findByClienteId(UUID clienteId);
    Optional<Conta> findFirstByClienteIdAndStatus(UUID clienteId, Conta.StatusConta status);
    boolean existsByAgenciaAndNumeroAndDigito(String agencia, String numero, String digito);
}
