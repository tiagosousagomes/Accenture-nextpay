package com.nextpay.repository;

import com.nextpay.entity.ResgateCashback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ResgateCashbackRepository extends JpaRepository<ResgateCashback, UUID> {
    List<ResgateCashback> findByClienteIdOrderByCriadoEmDesc(UUID clienteId);
}
