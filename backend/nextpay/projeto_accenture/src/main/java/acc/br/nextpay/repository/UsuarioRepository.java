package acc.br.nextpay.repository;

import acc.br.nextpay.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCpfCnpj(String cpfCnpj);

    Optional<Usuario> findByTokenConfirmacao(String token);

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCpfCnpj(String cpfCnpj);
}