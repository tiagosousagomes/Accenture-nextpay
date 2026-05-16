package acc.br.nextpay.repository;

import acc.br.nextpay.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByQuantidadeEstoqueGreaterThan(Integer quantidade);

    List<Produto> findByVendedorId(Long vendedorId);
}