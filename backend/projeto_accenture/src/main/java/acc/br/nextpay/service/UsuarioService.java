package acc.br.nextpay.service;

import acc.br.nextpay.exception.NegocioException;
import acc.br.nextpay.exception.RecursoNaoEncontradoException;
import acc.br.nextpay.model.ContaCorrente;
import acc.br.nextpay.model.Endereco;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.model.enums.StatusConta;
import acc.br.nextpay.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
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
    private final EmailService emailService;

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

        String token = String.valueOf((int) (Math.random() * 900000) + 100000);
        usuario.setTokenConfirmacao(token);
        usuario.setEmailConfirmado(false);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        // Enviar email de forma assíncrona (não bloqueia a resposta da API)
        enviarEmailConfirmacaoAsync(usuarioSalvo.getEmail(), usuarioSalvo.getNome(), token);

        return usuarioSalvo;
    }

    @Async
    public void enviarEmailConfirmacaoAsync(String email, String nome, String token) {
        try {
            emailService.enviarEmail(
                email,
                "Código de confirmação - NextPay",
                "Olá, " + nome + "!\n\n" +
                "Seu código de confirmação é:\n\n" +
                token + "\n\n" +
                "Digite esse código na tela de confirmação para ativar sua conta."
            );
        } catch (Exception e) {
            System.out.println("\n==================================================");
            System.out.println("  [DEV] EMAIL NÃO ENVIADO - USE ESTE CÓDIGO:");
            System.out.println("  Email : " + email);
            System.out.println("  Código: " + token);
            System.out.println("  Causa : " + e.getMessage());
            System.out.println("==================================================\n");
        }
    }

    @Transactional
    public void confirmarCodigoEmail(String email, String codigo) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        if (usuario.isEmailConfirmado()) {
            throw new NegocioException("E-mail já confirmado.");
        }

        if (usuario.getTokenConfirmacao() == null ||
                !usuario.getTokenConfirmacao().equals(codigo)) {
            throw new NegocioException("Código de confirmação inválido.");
        }

        usuario.setEmailConfirmado(true);
        usuario.setTokenConfirmacao(null);
        usuarioRepository.save(usuario);

        // Enviar email de sucesso de forma assíncrona
        enviarEmailSucessoAsync(usuario.getEmail(), usuario.getNome());
    }

    @Async
    public void enviarEmailSucessoAsync(String email, String nome) {
        try {
            emailService.enviarEmail(
                email,
                "Conta criada com sucesso - NextPay",
                "Olá, " + nome + "!\n\n" +
                "Sua conta foi confirmada e criada com sucesso.\n\n" +
                "Agora você já pode acessar o NextPay."
            );
        } catch (Exception e) {
            System.out.println("Erro ao enviar e-mail de conta criada: " + e.getMessage());
        }
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
