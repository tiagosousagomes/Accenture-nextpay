package acc.br.nextpay.service;

import acc.br.nextpay.exception.NegocioException;
import acc.br.nextpay.exception.RecursoNaoEncontradoException;
import acc.br.nextpay.model.ContaCorrente;
import acc.br.nextpay.model.Endereco;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.model.enums.StatusConta;
import acc.br.nextpay.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ViaCepService viaCepService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario cadastrarUsuario(Usuario usuario, String cep, String numero) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new NegocioException("Já existe um usuário cadastrado com este e-mail.");
        }

        if (usuarioRepository.existsByCpfCnpj(usuario.getCpfCnpj())) {
            throw new NegocioException("Já existe um usuário cadastrado com este CPF/CNPJ.");
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        Endereco endereco = viaCepService.buscarEnderecoPorCep(cep);
        if (endereco != null) {
            endereco.setNumero(numero);
            usuario.setEndereco(endereco);
        }

        ContaCorrente conta = ContaCorrente.builder()
                .numeroConta(UUID.randomUUID().toString().substring(0, 8))
                .saldo(BigDecimal.ZERO)
                .status(StatusConta.ATIVO)
                .usuario(usuario)
                .build();

        usuario.setConta(conta);

        return usuarioRepository.save(usuario);
    }

    public Usuario login(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NegocioException("E-mail ou senha inválidos."));

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            throw new NegocioException("E-mail ou senha inválidos.");
        }

        return usuario;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    @Transactional
    public void excluirConta(Long usuarioId, Long solicitanteId) {
        if (!usuarioId.equals(solicitanteId)) {
            throw new NegocioException("Você só pode excluir sua própria conta.");
        }

        Usuario usuario = buscarPorId(usuarioId);
        usuarioRepository.delete(usuario);
    }

    @Transactional
    public Usuario atualizarUsuario(Long id, Long solicitanteId, Usuario dadosAtualizados) {
        if (!id.equals(solicitanteId)) {
            throw new NegocioException("Você só pode atualizar sua própria conta.");
        }

        Usuario usuario = buscarPorId(id);

        usuario.setNome(dadosAtualizados.getNome());

        if (dadosAtualizados.getFotoPerfil() != null) {
            usuario.setFotoPerfil(dadosAtualizados.getFotoPerfil());
        }

        return usuarioRepository.saveAndFlush(usuario);
    }
}
