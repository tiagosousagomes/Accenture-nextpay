package com.nextpay.service;

import com.nextpay.dto.ProdutoRequest;
import com.nextpay.entity.Produto;
import com.nextpay.exception.BusinessException;
import com.nextpay.exception.ResourceNotFoundException;
import com.nextpay.repository.ClienteRepository;
import com.nextpay.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;

    public ProdutoService(ProdutoRepository produtoRepository,
                          ClienteRepository clienteRepository) {
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public Produto criar(ProdutoRequest req) {
        var vendedor = clienteRepository.findById(req.vendedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendedor não encontrado: " + req.vendedorId()));

        var produto = Produto.builder()
                .vendedor(vendedor)
                .nome(req.nome())
                .preco(req.preco())
                .estoque(req.estoque())
                .imagemUrl(req.imagemUrl())
                .ativo(true)
                .build();

        return produtoRepository.save(produto);
    }

    @Transactional(readOnly = true)
    public Produto buscar(UUID id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<Produto> listarPorVendedor(UUID vendedorId) {
        if (!clienteRepository.existsById(vendedorId)) {
            throw new ResourceNotFoundException("Vendedor não encontrado: " + vendedorId);
        }
        return produtoRepository.findByVendedorIdAndAtivoTrue(vendedorId);
    }

    @Transactional(readOnly = true)
    public List<Produto> listarTodos() {
        return produtoRepository.findByAtivoTrue();
    }

    @Transactional(readOnly = true)
    public List<Produto> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCaseAndAtivoTrue(nome);
    }

    @Transactional
    public Produto atualizar(UUID id, ProdutoRequest req) {
        var produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));

        if (!produto.getAtivo()) {
            throw new BusinessException("Não é possível atualizar um produto inativo");
        }

        produto.setNome(req.nome());
        produto.setPreco(req.preco());
        produto.setEstoque(req.estoque());
        produto.setImagemUrl(req.imagemUrl());

        return produtoRepository.save(produto);
    }

    @Transactional
    public void desativar(UUID id) {
        var produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }
}
