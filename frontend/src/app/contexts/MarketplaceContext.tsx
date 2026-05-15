import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { Produto, Pedido, CarrinhoItem, StatusPedido } from '../types';
import { useAuth } from './AuthContext';
import { useBank } from './BankContext';
import { useRewards } from './RewardsContext';
import api from '../services/api';

interface MarketplaceContextType {
  produtos: Produto[];
  getProdutosByVendedor: (vendedorId: string) => Produto[];
  carrinho: CarrinhoItem[];
  pedidos: Pedido[];
  adicionarAoCarrinho: (produto: Produto, quantidade: number, idPedido?: string) => Promise<boolean>;
  removerDoCarrinho: (produtoId: string) => Promise<void>;
  atualizarQuantidade: (produtoId: string, quantidade: number) => Promise<void>;
  limparCarrinho: () => void;
  confirmarPagamento: (itens: CarrinhoItem[]) => Promise<boolean>;
  confirmarPagamentoPedido: (pedidoId: string) => Promise<boolean>;
  criarPedido: () => Promise<string | null>;
  cancelarPedido: (pedidoId: string) => Promise<boolean>;
  pagarPedido: (pedidoId: string) => boolean;
  adicionarProduto: (produto: Partial<Produto>) => Promise<boolean>;
  atualizarProduto: (id: string, produto: Partial<Produto>) => Promise<boolean>;
  removerProduto: (id: string) => Promise<boolean>;
  refreshProdutos: () => void;
  getPedidosByCliente: (clienteId: string) => Pedido[];
  getPedidosByVendedor: (vendedorId: string) => Pedido[];
  getAllPedidos: () => Pedido[];
  refreshPedidos: () => void;
  lojaAberta: boolean;
  toggleLoja: () => void;
  isLoading: boolean;
}

const MarketplaceContext = createContext<MarketplaceContextType | undefined>(undefined);

export const useMarketplace = () => {
  const context = useContext(MarketplaceContext);
  if (!context) {
    throw new Error('useMarketplace must be used within MarketplaceProvider');
  }
  return context;
};

export const MarketplaceProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { user } = useAuth();
  const [produtos, setProdutos] = useState<Produto[]>([]);
  const [carrinho, setCarrinho] = useState<CarrinhoItem[]>([]);
  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [lojaAberta, setLojaAberta] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const { transferir, pix } = useBank();
  const { adicionarTransacaoOutroUsuario } = useRewards();

  const loadProdutos = async () => {
    setIsLoading(true);
    try {
      const response = await api.get('/api/produtos');
      const data = response.data;
      const mapped = data.map((p: any) => ({
        id: (p.id || '').toString(),
        nome: p.nome || 'Produto sem nome',
        descricao: p.descricao || '',
        preco: p.preco || 0,
        estoque: p.quantidadeEstoque || 0,
        imagem: p.fotoProduto || '',
        vendedorId: (p.vendedor?.id || '').toString(),
        vendedorNome: p.vendedor?.nome || 'Loja Parceira',
      }));
      setProdutos(mapped);
    } catch (err) {
      console.error('Erro ao carregar produtos:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const loadPedidos = async () => {
    if (!user) return;
    setIsLoading(true);
    try {
      const response = await api.get('/api/pedidos');
      const data = response.data;
      const mapped = data.map((p: any) => ({
        id: (p.id || '').toString(),
        clienteId: (p.comprador?.id || '').toString(),
        vendedorId: (p.vendedor?.id || '').toString(),
        vendedorNome: p.vendedor?.nome || 'Loja Parceira',
        produtoNome: p.produto?.nome || 'Produto',
        itens: [{
          produtoId: (p.produto?.id || '').toString(),
          quantidade: p.quantidade || 0,
          precoUnitario: p.produto?.preco || 0,
        }],
        total: p.valorTotal || 0,
        status: p.status === 'RESERVADO' ? 'RESERVED'
          : p.status === 'FINALIZADO' ? 'PAID'
          : p.status === 'CANCELADO' ? 'CANCELED'
          : 'RESERVED',
        dataCriacao: p.dataPedido || new Date().toISOString(),
        dataPagamento: p.status === 'FINALIZADO' ? p.dataPedido : undefined,
      }));
      setPedidos(mapped);
    } catch (err) {
      console.error('Erro ao carregar pedidos:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadProdutos();
    const storedLojaStatus = localStorage.getItem('marketpay_loja_aberta');
    if (storedLojaStatus !== null) setLojaAberta(JSON.parse(storedLojaStatus));
  }, []);

  useEffect(() => {
    if (user) loadPedidos();
  }, [user]);

  const adicionarAoCarrinho = async (produto: Produto, quantidade: number): Promise<boolean> => {
    if (!user) return false;
    setIsLoading(true);
    try {
      const response = await api.post('/api/pedidos/reservar', {
        produtoId: parseInt(produto.id),
        quantidade,
      });
      const resData = response.data;
      const novoIdPedido = (resData.id || '').toString();

      const itemExistente = carrinho.find(item => item.produto.id === produto.id);
      if (itemExistente) {
        setCarrinho(carrinho.map(item =>
          item.produto.id === produto.id
            ? { ...item, quantidade: item.quantidade + quantidade, idPedido: novoIdPedido }
            : item
        ));
      } else {
        setCarrinho([...carrinho, { idPedido: novoIdPedido, produto, quantidade }]);
      }
      await loadProdutos();
      return true;
    } catch (err) {
      console.error('Erro ao adicionar ao carrinho:', err);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const removerDoCarrinho = async (produtoId: string) => {
    const item = carrinho.find(i => i.produto.id === produtoId);
    setIsLoading(true);
    if (item?.idPedido) {
      try {
        await api.put(`/api/pedidos/${item.idPedido}/cancelar`);
      } catch (err) {
        console.error('Erro ao cancelar reserva:', err);
      }
    }
    setCarrinho(carrinho.filter(item => item.produto.id !== produtoId));
    await loadProdutos();
    setIsLoading(false);
  };

  const atualizarQuantidade = async (produtoId: string, quantidade: number) => {
    if (quantidade <= 0) {
      await removerDoCarrinho(produtoId);
    } else {
      setCarrinho(carrinho.map(item =>
        item.produto.id === produtoId ? { ...item, quantidade } : item
      ));
    }
  };

  const limparCarrinho = () => setCarrinho([]);

  const confirmarPagamento = async (itens: CarrinhoItem[]): Promise<boolean> => {
    if (!user) return false;
    setIsLoading(true);
    try {
      for (const item of itens) {
        await api.put(`/api/pedidos/${item.idPedido}/confirmar`);
      }
      await loadPedidos();
      await loadProdutos();
      limparCarrinho();
      window.dispatchEvent(new Event('atualizar-conta'));
      return true;
    } catch (err) {
      console.error('Erro ao confirmar pagamento:', err);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const confirmarPagamentoPedido = async (pedidoId: string): Promise<boolean> => {
    if (!user) return false;
    setIsLoading(true);
    try {
      await api.put(`/api/pedidos/${pedidoId}/confirmar`);
      await loadPedidos();
      await loadProdutos();
      window.dispatchEvent(new Event('atualizar-conta'));

      // Adicionar transação para o vendedor
      const pedido = pedidos.find(p => p.id === pedidoId);
      if (pedido && pedido.vendedorId) {
        adicionarTransacaoOutroUsuario(pedido.vendedorId, 'venda', pedido.total);
      }

      return true;
    } catch (err) {
      console.error('Erro ao confirmar pagamento do pedido:', err);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const criarPedido = async (): Promise<string | null> => {
    if (!user || carrinho.length === 0) return null;
    const novoPedido: Pedido = {
      id: `pedido_${Date.now()}`,
      clienteId: user.id,
      vendedorId: carrinho[0]?.produto.vendedorId || '',
      vendedorNome: carrinho[0]?.produto.vendedorNome || 'Loja Parceira',
      itens: carrinho.map(item => ({
        produtoId: item.produto.id,
        quantidade: item.quantidade,
        precoUnitario: item.produto.preco,
      })),
      total: carrinho.reduce((sum, item) => sum + item.produto.preco * item.quantidade, 0),
      status: 'RESERVED',
      dataCriacao: new Date().toISOString(),
    };
    setPedidos([...pedidos, novoPedido]);
    await loadPedidos();
    limparCarrinho();
    return novoPedido.id;
  };

  const cancelarPedido = async (pedidoId: string): Promise<boolean> => {
    setIsLoading(true);
    try {
      await api.put(`/api/pedidos/${pedidoId}/cancelar`);
      await loadPedidos();
      await loadProdutos();
      return true;
    } catch (err) {
      console.error('Erro ao cancelar pedido:', err);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const pagarPedido = (pedidoId: string): boolean => {
    const pedido = pedidos.find(p => p.id === pedidoId);
    if (!pedido || pedido.status !== 'RESERVED') return false;
    setPedidos(pedidos.map(p =>
      p.id === pedidoId
        ? { ...p, status: 'PAID' as StatusPedido, dataPagamento: new Date().toISOString() }
        : p
    ));
    return true;
  };

  const adicionarProduto = async (produto: Partial<Produto>): Promise<boolean> => {
    if (!user) return false;
    setIsLoading(true);
    try {
      await api.post('/api/produtos', {
        nome: produto.nome,
        descricao: produto.descricao || '',
        preco: produto.preco,
        quantidadeEstoque: produto.estoque,
        fotoProduto: produto.imagem || '',
      });
      await loadProdutos();
      return true;
    } catch (err) {
      console.error('Erro ao adicionar produto:', err);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const atualizarProduto = async (id: string, produtoAtualizado: Partial<Produto>): Promise<boolean> => {
    if (!user) return false;
    setIsLoading(true);
    try {
      await api.put(`/api/produtos/${id}`, {
        nome: produtoAtualizado.nome,
        descricao: produtoAtualizado.descricao || '',
        preco: produtoAtualizado.preco,
        quantidadeEstoque: produtoAtualizado.estoque,
        fotoProduto: produtoAtualizado.imagem || '',
      });
      await loadProdutos();
      return true;
    } catch (err) {
      console.error('Erro ao atualizar produto:', err);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const removerProduto = async (id: string): Promise<boolean> => {
    if (!user) return false;
    setIsLoading(true);
    try {
      await api.delete(`/api/produtos/${id}`);
      await loadProdutos();
      return true;
    } catch (err) {
      console.error('Erro ao remover produto:', err);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const getProdutosByVendedor = (vendedorId: string) =>
    produtos.filter(p => (p.vendedorId || '').toString() === (vendedorId || '').toString());

  const getPedidosByCliente = (clienteId: string) =>
    pedidos.filter(p => p.clienteId === clienteId);

  const getPedidosByVendedor = (vendedorId: string) =>
    pedidos.filter(p => p.vendedorId === vendedorId);

  const getAllPedidos = () => pedidos;

  const toggleLoja = () => {
    const novoStatus = !lojaAberta;
    setLojaAberta(novoStatus);
    localStorage.setItem('marketpay_loja_aberta', JSON.stringify(novoStatus));
  };

  return (
    <MarketplaceContext.Provider value={{
      produtos,
      getProdutosByVendedor,
      carrinho,
      pedidos,
      adicionarAoCarrinho,
      removerDoCarrinho,
      atualizarQuantidade,
      limparCarrinho,
      criarPedido,
      cancelarPedido,
      pagarPedido,
      adicionarProduto,
      atualizarProduto,
      removerProduto,
      refreshProdutos: loadProdutos,
      confirmarPagamento,
      confirmarPagamentoPedido,
      getPedidosByCliente,
      getPedidosByVendedor,
      getAllPedidos,
      refreshPedidos: loadPedidos,
      lojaAberta,
      toggleLoja,
      isLoading,
    }}>
      {children}
    </MarketplaceContext.Provider>
  );
};
