package com.nextpay.repository;

import com.nextpay.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, UUID> {
    List<Produto> findByAtivoTrue();
    List<Produto> findByVendedorIdAndAtivoTrue(UUID vendedorId);
    List<Produto> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome);
}
