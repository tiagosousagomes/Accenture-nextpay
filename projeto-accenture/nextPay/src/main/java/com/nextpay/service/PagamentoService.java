package com.nextpay.service;

import com.nextpay.dto.PagamentoRequest;
import com.nextpay.entity.*;
import com.nextpay.exception.BusinessException;
import com.nextpay.exception.ResourceNotFoundException;
import com.nextpay.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PagamentoService {

    private final PedidoRepository pedidoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;
    private final CashbackService cashbackService;

    public PagamentoService(PedidoRepository pedidoRepository,
                            PagamentoRepository pagamentoRepository,
                            ContaRepository contaRepository,
                            TransacaoRepository transacaoRepository,
                            CashbackService cashbackService) {
        this.pedidoRepository = pedidoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
        this.cashbackService = cashbackService;
    }

    /**
     * Pagar um Pedido:
     *  - Pedido precisa estar CRIADO e ainda sem Pagamento.
     *  - A conta origem precisa pertencer ao comprador do pedido.
     *  - Debita o valor total da conta, cria uma Transacao(COMPRA, MARKETPLACE)
     *    associada ao Pagamento, marca Pagamento CONCLUIDO e Pedido PAGO.
     */
    @Transactional
    public Pagamento pagar(UUID pedidoId, PagamentoRequest req) {
        var pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + pedidoId));

        if (pedido.getStatus() != Pedido.StatusPedido.CRIADO) {
            throw new BusinessException("Pedido não está disponível para pagamento (status: " + pedido.getStatus() + ")");
        }
        if (pagamentoRepository.findByPedidoId(pedidoId).isPresent()) {
            throw new BusinessException("Pedido já possui pagamento registrado");
        }

        var contaOrigem = contaRepository.findById(req.contaOrigemId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + req.contaOrigemId()));

        if (!contaOrigem.getCliente().getId().equals(pedido.getComprador().getId())) {
            throw new BusinessException("Conta origem não pertence ao comprador do pedido");
        }
        if (contaOrigem.getStatus() != Conta.StatusConta.ATIVA) {
            throw new BusinessException("Conta origem não está ativa");
        }

        BigDecimal valor = pedido.getValorTotal();
        BigDecimal disponivel = contaOrigem.getSaldo().add(contaOrigem.getLimite());
        if (disponivel.compareTo(valor) < 0) {
            throw new BusinessException("Saldo insuficiente para pagar o pedido");
        }

        var pagamento = Pagamento.builder()
                .pedido(pedido)
                .contaOrigem(contaOrigem)
                .metodo(req.metodo())
                .status(Pagamento.StatusPagamento.PENDENTE)
                .build();
        pagamento = pagamentoRepository.save(pagamento);

        contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(valor));
        contaRepository.save(contaOrigem);

        var transacao = Transacao.builder()
                .conta(contaOrigem)
                .pagamento(pagamento)
                .tipo(Transacao.TipoTransacao.COMPRA)
                .categoria(Transacao.CategoriaTransacao.MARKETPLACE)
                .valor(valor)
                .status(Transacao.StatusTransacao.CONCLUIDA)
                .descricao("Pagamento pedido " + pedido.getId())
                .valorCashbackGerado(BigDecimal.ZERO)
                .build();
        transacao = transacaoRepository.save(transacao);

        cashbackService.calcularEAplicarCashback(transacao);

        pedido.setCashbackGerado(transacao.getValorCashbackGerado());
        pedido.setStatus(Pedido.StatusPedido.PAGO);
        pedidoRepository.save(pedido);

        pagamento.setStatus(Pagamento.StatusPagamento.CONCLUIDO);
        pagamento.setPagoEm(LocalDateTime.now());
        return pagamentoRepository.save(pagamento);
    }
}
