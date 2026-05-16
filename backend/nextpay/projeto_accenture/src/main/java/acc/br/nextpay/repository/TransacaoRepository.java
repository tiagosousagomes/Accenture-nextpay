package acc.br.nextpay.repository;

import acc.br.nextpay.model.Transacao;
import acc.br.nextpay.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    List<Transacao> findByUsuarioOrderByDataDesc(Usuario usuario);
    List<Transacao> findByUsuarioIdOrderByDataDesc(Long usuarioId);
}
