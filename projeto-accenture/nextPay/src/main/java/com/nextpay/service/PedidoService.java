package com.nextpay.service;

import com.nextpay.dto.ItemPedidoRequest;
import com.nextpay.dto.PedidoRequest;
import com.nextpay.entity.*;
import com.nextpay.exception.BusinessException;
import com.nextpay.exception.ResourceNotFoundException;
import com.nextpay.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;
    private final EnderecoRepository enderecoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProdutoRepository produtoRepository,
                         ClienteRepository clienteRepository,
                         EnderecoRepository enderecoRepository,
                         ItemPedidoRepository itemPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
        this.enderecoRepository = enderecoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    @Transactional
    public Pedido criar(PedidoRequest req) {
        var comprador = clienteRepository.findById(req.compradorId())
                .orElseThrow(() -> new ResourceNotFoundException("Comprador não encontrado: " + req.compradorId()));

        var endereco = enderecoRepository.findById(req.enderecoEntregaId())
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado: " + req.enderecoEntregaId()));

        if (!endereco.getCliente().getId().equals(comprador.getId())) {
            throw new BusinessException("Endereço de entrega não pertence ao comprador");
        }

        var pedido = Pedido.builder()
                .comprador(comprador)
                .enderecoEntrega(endereco)
                .status(Pedido.StatusPedido.CRIADO)
                .valorTotal(BigDecimal.ZERO)
                .cashbackGerado(BigDecimal.ZERO)
                .build();
        pedido = pedidoRepository.save(pedido);

        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedidoRequest itemReq : req.itens()) {
            var produto = produtoRepository.findById(itemReq.produtoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + itemReq.produtoId()));

            if (!produto.getAtivo()) {
                throw new BusinessException("Produto inativo: " + produto.getNome());
            }
            if (produto.getEstoque() == 0) {
                throw new BusinessException("Produto sem estoque: " + produto.getNome());
            }
            if (produto.getEstoque() < itemReq.quantidade()) {
                throw new BusinessException("Estoque insuficiente para: " + produto.getNome()
                        + " (disponível: " + produto.getEstoque() + ")");
            }

            // Snapshot de preço
            var item = ItemPedido.builder()
                    .pedido(pedido)
                    .produto(produto)
                    .quantidade(itemReq.quantidade())
                    .precoUnitario(produto.getPreco())
                    .build();
            itemPedidoRepository.save(item);
            pedido.getItens().add(item);

            // Reserva estoque
            produto.setEstoque(produto.getEstoque() - itemReq.quantidade());
            produtoRepository.save(produto);

            total = total.add(produto.getPreco().multiply(BigDecimal.valueOf(itemReq.quantidade())));
        }

        pedido.setValorTotal(total);
        return pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public Pedido buscar(UUID id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + id));
    }
}
