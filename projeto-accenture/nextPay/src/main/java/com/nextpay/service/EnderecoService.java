package com.nextpay.service;

import com.nextpay.dto.EnderecoRequest;
import com.nextpay.dto.ViaCepResponse;
import com.nextpay.entity.Cliente;
import com.nextpay.entity.Endereco;
import com.nextpay.exception.ResourceNotFoundException;
import com.nextpay.repository.ClienteRepository;
import com.nextpay.repository.EnderecoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final ClienteRepository clienteRepository;
    private final ViaCepService viaCepService;

    public EnderecoService(EnderecoRepository enderecoRepository,
                           ClienteRepository clienteRepository,
                           ViaCepService viaCepService) {
        this.enderecoRepository = enderecoRepository;
        this.clienteRepository = clienteRepository;
        this.viaCepService = viaCepService;
    }

    @Transactional
    public Endereco criar(UUID clienteId, EnderecoRequest req) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: " + clienteId));

        ViaCepResponse viacep = viaCepService.buscar(req.cep());

        var endereco = Endereco.builder()
                .cliente(cliente)
                .cep(req.cep())
                .logradouro(req.logradouro() != null ? req.logradouro() : viacep.logradouro())
                .numero(req.numero())
                .complemento(req.complemento() != null ? req.complemento() : viacep.complemento())
                .cidade(req.cidade() != null ? req.cidade() : viacep.localidade())
                .uf(req.uf() != null ? req.uf().toUpperCase() : viacep.uf())
                .tipo(req.tipo())
                .build();

        return enderecoRepository.save(endereco);
    }

    @Transactional(readOnly = true)
    public List<Endereco> listarPorCliente(UUID clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new ResourceNotFoundException("Cliente não encontrado: " + clienteId);
        }
        return enderecoRepository.findByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public Endereco buscar(UUID id) {
        return enderecoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado: " + id));
    }

    @Transactional
    public Endereco atualizar(UUID id, EnderecoRequest req) {
        var endereco = enderecoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado: " + id));

        ViaCepResponse viacep = viaCepService.buscar(req.cep());

        endereco.setCep(req.cep());
        endereco.setLogradouro(req.logradouro() != null ? req.logradouro() : viacep.logradouro());
        endereco.setNumero(req.numero());
        endereco.setComplemento(req.complemento() != null ? req.complemento() : viacep.complemento());
        endereco.setCidade(req.cidade() != null ? req.cidade() : viacep.localidade());
        endereco.setUf(req.uf() != null ? req.uf().toUpperCase() : viacep.uf());
        endereco.setTipo(req.tipo());

        return enderecoRepository.save(endereco);
    }

    @Transactional
    public void remover(UUID id) {
        if (!enderecoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Endereço não encontrado: " + id);
        }
        enderecoRepository.deleteById(id);
    }
}
