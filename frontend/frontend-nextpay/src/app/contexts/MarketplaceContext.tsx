import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { Produto, Pedido, CarrinhoItem, StatusPedido } from '../types';
import { useAuth } from './AuthContext';
import { useBank } from './BankContext';
import { atualizarSistema } from '../utils/eventBus';

interface MarketplaceContextType {
  produtos: Produto[];
  getProdutosByVendedor: (vendedorId: string) => Produto[];
  carrinho: CarrinhoItem[];
  pedidos: Pedido[];
  adicionarAoCarrinho: (produto: Produto, quantidade: number, idPedido?: string) => Promise<boolean>;
  removerDoCarrinho: (produtoId: string) => Promise<void>;
  atualizarQuantidade: (produtoId: string, quantidade: number) => Promise<void>;
  limparCarrinho: () => void;
  confirmarPagamento: (itens: CarrinhoItem[], moedasUsadas?: number) => Promise<boolean>;
  confirmarPagamentoPedido: (pedidoId: string, moedasUsadas?: number) => Promise<boolean>;
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
  setCarrinho: React.Dispatch<React.SetStateAction<CarrinhoItem[]>>;

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

  const loadProdutos = async () => {
    setIsLoading(true);
    try {
      const response = await fetch('http://localhost:8080/api/produtos');
      if (response.ok) {
        const data = await response.json();
        console.log(data);
        // Mapear do backend para o frontend
        const mapped = data.map((p: any) => {
          // Tentar capturar o ID do vendedor de várias formas comuns
          const vId = (p.vendedorId || p.vendedor?.id || p.usuarioId || '').toString();
          return {
            id: (p.id || '').toString(),
            nome: p.nome || 'Produto sem nome',
            descricao: p.descricao || '',
            preco: p.preco || 0,
            estoque: p.quantidadeEstoque || 0,
            imagem: p.fotoProduto || '',
            vendedorId: vId,
            vendedorNome: p.vendedor?.nome || (typeof p.vendedor === 'string' ? p.vendedor : 'Loja Parceira'),
          };
        });
        setProdutos(mapped);
      }
    } catch (err) {
      console.error('Erro ao carregar produtos:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const loadPedidos = async () => {
    setIsLoading(true);
    try {
      const response = await fetch('http://localhost:8080/api/pedidos');
      if (response.ok) {
        const data = await response.json();

        // Mapear cada pedido do backend para o formato do frontend
        const mapped = data.map((p: any) => {
          const cId = (p.comprador?.id || p.compradorId || p.clienteId || '').toString();
          const vId = (p.vendedor?.id || p.vendedorId || '').toString();

          return {
            id: (p.id || '').toString(),
            clienteId: cId,
            vendedorId: vId,
            vendedorNome: p.vendedor?.nome || 'Loja Parceira',
            produtoNome: p.produto?.nome || 'Produto',
            itens: [{
              produtoId: (p.produto?.id || p.produtoId || '').toString(),
              quantidade: p.quantidade || 0,
              precoUnitario: p.produto?.preco || 0
            }],
            total: p.valorTotal || 0,
            status: p.status === 'RESERVADO' ? 'RESERVED' :
              p.status === 'FINALIZADO' ? 'PAID' :
                p.status === 'CANCELADO' ? 'CANCELED' :
                  p.status === 'EXPIRADO' ? 'EXPIRED' : 'RESERVED',
            dataCriacao: p.dataPedido || new Date().toISOString(),
            dataPagamento: (p.status === 'FINALIZADO' || p.status === 'PAGO') ? p.dataPedido : undefined
          };
        });
        setPedidos(mapped);
      }
    } catch (err) {
      console.error('Erro ao carregar pedidos:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    const interval = setInterval(async () => {

      const agora = new Date().getTime();

      const pedidosExpirados = pedidos.filter(p => {
        if (p.status !== 'RESERVED') return false;

        const dataPedido = new Date(p.dataCriacao).getTime();

        const tempoLimite = 2 * 60 * 1000;

        return agora - dataPedido > tempoLimite;
      });

      if (pedidosExpirados.length > 0) {

        for (const pedido of pedidosExpirados) {

          try {

            await fetch(`http://localhost:8080/api/pedidos/${pedido.id}/cancelar`, {
              method: 'PUT'
            });

          } catch (err) {
            console.error('Erro ao cancelar pedido expirado:', err);
          }

        }

        setCarrinho((carrinhoAtual) => carrinhoAtual.filter((item) => !pedidosExpirados.some((pedido) => pedido.id === item.idPedido))
        );

        await loadPedidos();
        await loadProdutos();

        atualizarSistema();
      }

    }, 10000);

    return () => clearInterval(interval);

  }, [pedidos]);


  useEffect(() => {
    loadProdutos();
    loadPedidos();

    // Carregar status da loja
    const storedLojaStatus = localStorage.getItem('marketpay_loja_aberta');
    if (storedLojaStatus !== null) {
      setLojaAberta(JSON.parse(storedLojaStatus));
    }
  }, []);

  useEffect(() => {
    const atualizar = () => {
      loadProdutos();
      loadPedidos();
    };

    window.addEventListener('atualizar-sistema', atualizar);

    return () => {
      window.removeEventListener('atualizar-sistema', atualizar);
    };
  }, []);

  const adicionarAoCarrinho = async (produto: Produto, quantidade: number, idPedido?: string): Promise<boolean> => {
    if (!user) return false;
    setIsLoading(true);
    try {
      // Reservar no backend imediatamente
      const payload = {
        compradorId: parseInt(user.id),
        produtoId: parseInt(produto.id),
        quantidade: quantidade
      };

      const response = await fetch('http://localhost:8080/api/pedidos/reservar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        const errorMsg = await response.text();
        console.error('Erro ao reservar produto:', errorMsg);
        return false;
      }

      // Capturar o ID do pedido gerado pelo backend
      const resData = await response.json();
      const novoIdPedido = (resData.id || resData.pedidoId || '').toString();

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

      // Sincronizar estoque local
      await loadProdutos();
      atualizarSistema();
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
        await fetch(`http://localhost:8080/api/pedidos/${item.idPedido}/cancelar`, {
          method: 'PUT'
        });
      } catch (err) {
        console.error('Erro ao cancelar reserva ao remover do carrinho:', err);
      }
    }

    setCarrinho(carrinho.filter(item => item.produto.id !== produtoId));
    await loadProdutos(); // Recarregar estoque
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

  const limparCarrinho = () => {
    setCarrinho([]);
  };

  const confirmarPagamento = async (itens: CarrinhoItem[], moedasUsadas: number = 0): Promise<boolean> => {
    if (!user) return false;

    setIsLoading(true);
    try {
      let moedasRestantes = moedasUsadas;
      for (const item of itens) {
        const valorEmMoedas = item.produto.preco * item.quantidade * 10;
        const moedasParaEsteItem = Math.min(moedasRestantes, valorEmMoedas);
        const response = await fetch(`http://localhost:8080/api/pedidos/${item.idPedido}/confirmar`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ moedasUsadas }),
        });
        if (!response.ok) return false;
        moedasRestantes -= moedasParaEsteItem;
      }

      await loadPedidos();
      await loadProdutos();
      limparCarrinho();

      atualizarSistema();

      return true;
    } catch (err) {
      console.error('Erro ao confirmar pagamento:', err);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const confirmarPagamentoPedido = async (pedidoId: string, moedasUsadas: number = 0): Promise<boolean> => {
    if (!user) return false;
    const pedido = pedidos.find(p => p.id === pedidoId);
    if (!pedido) return false;

    setIsLoading(true);
    try {
      // Confirmar (Muda status para Finalizado)
      const confirmResponse = await fetch(`http://localhost:8080/api/pedidos/${pedidoId}/confirmar`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ moedasUsadas }),
      });

      if (!confirmResponse.ok) return false;

      // Atualizar local
      await loadPedidos();
      await loadProdutos();

      atualizarSistema();

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

    // O pedido já foi reservado no backend no momento de "Adicionar ao Carrinho"
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
      total: carrinho.reduce((sum, item) => sum + (item.produto.preco * item.quantidade), 0),
      status: 'RESERVED',
      dataCriacao: new Date().toISOString(),
    };

    const todosPedidos = [...pedidos, novoPedido];
    setPedidos(todosPedidos);

    // Sincronizar com o backend
    await loadPedidos();

    limparCarrinho();
    return novoPedido.id;
  };

  const cancelarPedido = async (pedidoId: string): Promise<boolean> => {
    setIsLoading(true);
    try {
      const response = await fetch(`http://localhost:8080/api/pedidos/${pedidoId}/cancelar`, {
        method: 'PUT'
      });

      if (response.ok) {
        await loadPedidos();
        await loadProdutos();
        return true;
      }
      return false;
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

    const pedidosAtualizados = pedidos.map(p =>
      p.id === pedidoId
        ? { ...p, status: 'PAID' as StatusPedido, dataPagamento: new Date().toISOString() }
        : p
    );

    setPedidos(pedidosAtualizados);
    localStorage.setItem('marketpay_pedidos', JSON.stringify(pedidosAtualizados));

    return true;
  };

  const adicionarProduto = async (produto: Partial<Produto>): Promise<boolean> => {
    if (!user) return false;
    const currentUserId = user.id || (user as any).usuarioId;

    setIsLoading(true);
    try {
      const payload = {
        nome: produto.nome,
        descricao: produto.descricao || '',
        preco: produto.preco,
        quantidadeEstoque: produto.estoque,
        fotoProduto: produto.imagem || ''
      };

      const response = await fetch(`http://localhost:8080/api/produtos/usuario/${currentUserId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (response.ok) {
        await loadProdutos();
        atualizarSistema();
        return true;
      }
      return false;
    } catch (err) {
      console.error('Erro ao adicionar produto:', err);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const atualizarProduto = async (id: string, produtoAtualizado: Partial<Produto>): Promise<boolean> => {
    if (!user) return false;
    const currentUserId = user.id || (user as any).usuarioId;

    setIsLoading(true);
    try {
      const payload = {
        id: parseInt(id),
        nome: produtoAtualizado.nome,
        descricao: produtoAtualizado.descricao || '',
        preco: produtoAtualizado.preco,
        quantidadeEstoque: produtoAtualizado.estoque,
        fotoProduto: produtoAtualizado.imagem || ''
      };

      const response = await fetch(`http://localhost:8080/api/produtos/${id}/usuario/${currentUserId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (response.ok) {
        await loadProdutos();
        atualizarSistema();
        return true;
      }
      return false;
    } catch (err) {
      console.error('Erro ao atualizar produto:', err);
      return false;
    }
  };

  const removerProduto = async (id: string): Promise<boolean> => {
    if (!user) return false;
    const currentUserId = user.id || (user as any).usuarioId;

    setIsLoading(true);
    try {
      const response = await fetch(`http://localhost:8080/api/produtos/${id}/usuario/${currentUserId}`, {
        method: 'DELETE'
      });

      if (response.ok) {
        await loadProdutos();
        atualizarSistema();
        return true;
      }
      return false;
    } catch (err) {
      console.error('Erro ao remover produto:', err);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const getPedidosByCliente = (clienteId: string) => {
    return pedidos.filter(p => p.clienteId === clienteId);
  };

  const getPedidosByVendedor = (vendedorId: string) => {
    return pedidos.filter(p => p.vendedorId === vendedorId);
  };

  const getAllPedidos = () => {
    return pedidos;
  };

  const getProdutosByVendedor = (vendedorId: string) => {
    const idToMatch = vendedorId?.toString() || '';
    return produtos.filter(p => (p.vendedorId || '').toString() === idToMatch);
  };

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
      setCarrinho,
    }}>
      {children}
    </MarketplaceContext.Provider>
  );
};
