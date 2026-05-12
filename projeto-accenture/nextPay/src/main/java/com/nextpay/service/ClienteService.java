package com.nextpay.service;

import com.nextpay.dto.*;
import com.nextpay.entity.*;
import com.nextpay.exception.BusinessException;
import com.nextpay.exception.ResourceNotFoundException;
import com.nextpay.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.UUID;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final EnderecoRepository enderecoRepository;
    private final ContaRepository contaRepository;
    private final ViaCepService viaCepService;
    private final SecureRandom random = new SecureRandom();

    public ClienteService(ClienteRepository clienteRepository,
                          EnderecoRepository enderecoRepository,
                          ContaRepository contaRepository,
                          ViaCepService viaCepService) {
        this.clienteRepository = clienteRepository;
        this.enderecoRepository = enderecoRepository;
        this.contaRepository = contaRepository;
        this.viaCepService = viaCepService;
    }

    @Transactional


   
    public ClienteResponse criar(ClienteRequest req) {
        var cpfNormalizado = req.cpf().replaceAll("\\D", "");

        if (clienteRepository.existsByCpf(cpfNormalizado)) {
            throw new BusinessException("CPF já cadastrado");
        }
        if (clienteRepository.existsByEmail(req.email())) {
            throw new BusinessException("E-mail já cadastrado");
        }

        var clienteSalvo = clienteRepository.save(
                Cliente.builder()
                        .nome(req.nome())
                        .cpf(cpfNormalizado)
                        .email(req.email())
                        .senhaHash(hash(req.senha()))
                        .tipo(req.tipo())
                        .nivel(Cliente.NivelCliente.BRONZE)
                        .pontosAcumulados(0L)
                        .saldoCashback(java.math.BigDecimal.ZERO)
                        .build()
        );

        if (req.enderecos() != null) {
            for (EnderecoRequest er : req.enderecos()) {
                String logradouro = er.logradouro();
                String complemento = er.complemento();
                String cidade = er.cidade();
                String uf = er.uf();

                if (isBlank(logradouro) || isBlank(cidade) || isBlank(uf)) {
                    var viaCep = viaCepService.buscar(er.cep());
                    if (isBlank(logradouro)) logradouro = viaCep.logradouro();
                    if (isBlank(complemento)) complemento = viaCep.complemento();
                    if (isBlank(cidade)) cidade = viaCep.localidade();
                    if (isBlank(uf)) uf = viaCep.uf();
                }

                var end = Endereco.builder()
                        .cliente(clienteSalvo)
                        .cep(er.cep())
                        .logradouro(logradouro)
                        .numero(er.numero())
                        .complemento(complemento)
                        .cidade(cidade)
                        .uf(uf.toUpperCase())
                        .tipo(er.tipo())
                        .build();
                enderecoRepository.save(end);
            }
        }

        criarContaPadrao(clienteSalvo);

        var clienteAtualizado = clienteRepository.findById(clienteSalvo.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: " + clienteSalvo.getId()));
        return ClienteResponse.from(clienteAtualizado);
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(UUID id) {
        var cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: " + id));
        return ClienteResponse.from(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente buscarEntidade(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: " + id));
    }

    private void criarContaPadrao(Cliente cliente) {
        String agencia, numero, digito;
        do {
            agencia = "0001";
            numero = String.format("%08d", random.nextInt(100_000_000));
            digito = String.format("%02d", random.nextInt(100));
        } while (contaRepository.existsByAgenciaAndNumeroAndDigito(agencia, numero, digito));

        var conta = Conta.builder()
                .cliente(cliente)
                .agencia(agencia)
                .numero(numero)
                .digito(digito)
                .saldo(java.math.BigDecimal.ZERO)
                .limite(java.math.BigDecimal.ZERO)
                .status(Conta.StatusConta.ATIVA)
                .build();

        contaRepository.save(conta);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String hash(String senha) {
        // MVP: hash simples. Substituir por BCrypt em produção.
        return Integer.toHexString(senha.hashCode()) + "$mvp";
    }
}
