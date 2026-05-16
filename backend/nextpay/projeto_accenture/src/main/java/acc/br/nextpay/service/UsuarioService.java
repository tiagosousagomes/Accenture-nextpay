package acc.br.nextpay.service;

import acc.br.nextpay.model.ContaCorrente;
import acc.br.nextpay.model.Endereco;
import acc.br.nextpay.model.Usuario;
import acc.br.nextpay.model.enums.StatusConta;
import acc.br.nextpay.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private EmailService emailService;

    @Transactional
    public Usuario cadastrarUsuario(Usuario usuario, String cep, String numero) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("Já existe um usuário cadastrado com este e-mail.");
        }

        if (usuarioRepository.existsByCpfCnpj(usuario.getCpfCnpj())) {
            throw new RuntimeException("Já existe um usuário cadastrado com este CPF/CNPJ.");
        }

        // Hashing a senha antes de salvar
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

        try {
            emailService.enviarEmail(
                usuarioSalvo.getEmail(),
                "Código de confirmação - NextPay",
                "Olá, " + usuarioSalvo.getNome() + "!\n\n" +
                "Seu código de confirmação é:\n\n" +
                token + "\n\n" +
                "Digite esse código na tela de confirmação para ativar sua conta."
            );
        } catch (Exception e) {
            System.out.println("Erro ao enviar e-mail de confirmação: " + e.getMessage());
        }

        return usuarioSalvo;
    }

    public Usuario login(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("E-mail ou senha inválidos."));
        
        if (!usuario.isEmailConfirmado()) {
            throw new RuntimeException("Confirme seu e-mail antes de fazer login.");
        }

        // Comparando a senha fornecida com o hash no banco
        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            throw new RuntimeException("E-mail ou senha inválidos.");
        }

        return usuario;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }

    @Transactional
    public void excluirConta(Long usuarioId, boolean confirmar) {
        if (!confirmar) {
            throw new RuntimeException("Para excluir a conta, envie confirmar=true.");
        }

        Usuario usuario = buscarPorId(usuarioId);
        usuarioRepository.delete(usuario);
    }

    @Transactional
    public Usuario atualizarUsuario(Long id, Usuario dadosAtualizados) {
        Usuario usuario = buscarPorId(id);

        usuario.setNome(dadosAtualizados.getNome());

        if (dadosAtualizados.getFotoPerfil() != null) {
            usuario.setFotoPerfil(dadosAtualizados.getFotoPerfil());
        }

        return usuarioRepository.saveAndFlush(usuario);
    }

    @Transactional
    public void confirmarCodigoEmail(String email, String codigo) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (usuario.isEmailConfirmado()) {
            throw new RuntimeException("E-mail já confirmado.");
        }

        if (usuario.getTokenConfirmacao() == null ||
                !usuario.getTokenConfirmacao().equals(codigo)) {
            throw new RuntimeException("Código de confirmação inválido.");
        }

        usuario.setEmailConfirmado(true);
        usuario.setTokenConfirmacao(null);

        usuarioRepository.save(usuario);

        try {
            emailService.enviarEmail(
                    usuario.getEmail(),
                    "Conta criada com sucesso - NextPay",
                    "Olá, " + usuario.getNome() + "!\n\n" +
                    "Sua conta foi confirmada e criada com sucesso.\n\n" +
                    "Agora você já pode acessar o NextPay."
            );
        } catch (Exception e) {
            System.out.println("Erro ao enviar e-mail de conta criada: " + e.getMessage());
        }
    }

}