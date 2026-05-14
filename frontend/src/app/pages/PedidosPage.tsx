import React, { useState, useEffect } from 'react';
import { Layout } from '../components/Layout';
import { useMarketplace } from '../contexts/MarketplaceContext';
import { useAuth } from '../contexts/AuthContext';
import { useBank } from '../contexts/BankContext';
import { useRewards } from '../contexts/RewardsContext';
import {
  Package,
  CheckCircle,
  XCircle,
  Clock,
  Ban,
  Download,
  CreditCard,
  Wallet,
  TrendingUp,
  X,
  Search,
  ArrowRight,
  ShieldCheck,
  Filter,
} from 'lucide-react';
import { toast } from 'sonner';
import { jsPDF } from 'jspdf';
import { Pedido, StatusPedido } from '../types';

export const PedidosPage: React.FC = () => {
  const { user } = useAuth();
  const { 
    getPedidosByCliente, 
    produtos, 
    cancelarPedido, 
    confirmarPagamentoPedido,
    refreshPedidos 
  } = useMarketplace();
  const { conta, refreshConta } = useBank();
  const { adicionarMoedas, adicionarTransacao, calcularRecompensaCompra } = useRewards();

  useEffect(() => {
    refreshPedidos();
  }, []);

  const [abaAtiva, setAbaAtiva] = useState<'RESERVED' | 'PAID' | 'ALL'>('ALL');
  const [pagamentoModalAberto, setPagamentoModalAberto] = useState(false);
  const [pedidoSelecionado, setPedidoSelecionado] = useState<Pedido | null>(null);
  const [metodoPagamento, setMetodoPagamento] = useState<'pix' | 'transferencia' | null>(null);
  const [isProcessando, setIsProcessando] = useState(false);

  const todosPedidos = getPedidosByCliente(user?.id || '').sort((a, b) =>
    new Date(b.dataCriacao).getTime() - new Date(a.dataCriacao).getTime()
  );

  const pedidosFiltrados = todosPedidos.filter(p => 
    abaAtiva === 'ALL' ? true : p.status === abaAtiva
  );

  const handleCancelarPedido = async (pedidoId: string) => {
    const sucesso = await cancelarPedido(pedidoId);
    if (sucesso) {
      toast.success('Reserva cancelada e estoque liberado.');
    } else {
      toast.error('Não foi possível cancelar este pedido.');
    }
  };

  const handleAbrirPagamento = (pedido: Pedido) => {
    setPedidoSelecionado(pedido);
    setPagamentoModalAberto(true);
  };

  const handleConfirmarPagamento = async () => {
    if (!pedidoSelecionado || !metodoPagamento) return;

    if (!conta || pedidoSelecionado.total > conta.saldo) {
      toast.error('Saldo insuficiente na conta');
      return;
    }

    setIsProcessando(true);
    try {
      // O fluxo no context já faz: comprar (POST) -> confirmar (PUT)
      const sucesso = await confirmarPagamentoPedido(pedidoSelecionado.id);

      if (sucesso) {
        // Recompensas
        const moedas = calcularRecompensaCompra(pedidoSelecionado.total);
        adicionarTransacao('compra', pedidoSelecionado.total);
        adicionarMoedas(moedas);
        refreshConta();

        toast.success(
          <div className="flex items-center gap-2">
            <CheckCircle size={20} className="text-green-600" />
            <div>
              <p className="font-bold">Pagamento Finalizado!</p>
              <p className="text-xs">Seu pedido foi processado com sucesso.</p>
            </div>
          </div>
        );
        setPagamentoModalAberto(false);
        setPedidoSelecionado(null);
        setMetodoPagamento(null);
      } else {
        toast.error('Erro ao processar pagamento no servidor.');
      }
    } catch (err) {
      toast.error('Erro de conexão ao processar pagamento.');
    } finally {
      setIsProcessando(false);
    }
  };

  const gerarComprovante = (pedido: Pedido) => {
    const doc = new jsPDF();
    doc.setFontSize(22);
    doc.setTextColor(17, 94, 103); // Petróleo
    doc.text('MarketPay - Comprovante', 20, 20);
    
    doc.setFontSize(12);
    doc.setTextColor(80, 80, 80);
    doc.text(`Comprador: ${user?.nome} ${user?.sobrenome}`, 20, 35);
    doc.text(`Pedido ID: ${pedido.id}`, 20, 42);
    doc.text(`Data: ${new Date(pedido.dataCriacao).toLocaleString('pt-BR')}`, 20, 49);
    doc.text(`Status: FINALIZADO`, 20, 56);

    doc.setDrawColor(200, 200, 200);
    doc.line(20, 62, 190, 62);

    doc.setFontSize(14);
    doc.text('Itens Comprados', 20, 75);
    
    let y = 85;
    pedido.itens.forEach((item) => {
      const produto = produtos.find(p => p.id === item.produtoId);
      doc.setFontSize(11);
      doc.text(`${produto?.nome || 'Produto'} x${item.quantidade}`, 20, y);
      doc.text(`R$ ${(item.precoUnitario * item.quantidade).toFixed(2)}`, 160, y);
      y += 8;
    });

    doc.line(20, y + 5, 190, y + 5);
    doc.setFontSize(16);
    doc.text('Total Pago:', 20, y + 20);
    doc.text(`R$ ${pedido.total.toFixed(2)}`, 160, y + 20);

    doc.save(`marketpay-pedido-${pedido.id.slice(-6)}.pdf`);
    toast.success('Comprovante exportado com sucesso!');
  };

  const getStatusConfig = (status: StatusPedido) => {
    switch (status) {
      case 'RESERVED':
        return { 
          label: 'Pendente', 
          icon: <Clock size={16} />, 
          bg: 'bg-amber-50', 
          text: 'text-amber-700', 
          border: 'border-amber-200' 
        };
      case 'PAID':
        return { 
          label: 'Finalizado', 
          icon: <CheckCircle size={16} />, 
          bg: 'bg-green-50', 
          text: 'text-green-700', 
          border: 'border-green-200' 
        };
      case 'CANCELED':
        return { 
          label: 'Cancelado', 
          icon: <Ban size={16} />, 
          bg: 'bg-gray-50', 
          text: 'text-gray-600', 
          border: 'border-gray-200' 
        };
      case 'EXPIRED':
        return { 
          label: 'Expirado', 
          icon: <Clock size={16} />, 
          bg: 'bg-rose-50', 
          text: 'text-rose-700', 
          border: 'border-rose-200' 
        };
      default:
        return { 
          label: 'Desconhecido', 
          icon: <Package size={16} />, 
          bg: 'bg-gray-50', 
          text: 'text-gray-600', 
          border: 'border-gray-200' 
        };
    }
  };

  return (
    <Layout>
      <div className="max-w-6xl mx-auto space-y-8">
        {/* Header Section */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div>
            <h1 className="text-4xl font-black text-gray-900">Meus Pedidos</h1>
            <p className="text-gray-500 mt-2 font-medium">Gerencie suas reservas e histórico de compras.</p>
          </div>
          
          <div className="flex bg-white p-1 rounded-2xl shadow-sm border border-gray-100">
            <button
              onClick={() => setAbaAtiva('ALL')}
              className={`px-6 py-2.5 rounded-xl text-sm font-bold transition-all ${abaAtiva === 'ALL' ? 'bg-petroleo-600 text-white shadow-lg' : 'text-gray-500 hover:text-gray-800'}`}
            >
              Todos
            </button>
            <button
              onClick={() => setAbaAtiva('RESERVED')}
              className={`px-6 py-2.5 rounded-xl text-sm font-bold transition-all ${abaAtiva === 'RESERVED' ? 'bg-petroleo-600 text-white shadow-lg' : 'text-gray-500 hover:text-gray-800'}`}
            >
              Pendentes
            </button>
            <button
              onClick={() => setAbaAtiva('PAID')}
              className={`px-6 py-2.5 rounded-xl text-sm font-bold transition-all ${abaAtiva === 'PAID' ? 'bg-petroleo-600 text-white shadow-lg' : 'text-gray-500 hover:text-gray-800'}`}
            >
              Finalizados
            </button>
          </div>
        </div>

        {/* Content Section */}
        {pedidosFiltrados.length === 0 ? (
          <div className="bg-white rounded-3xl p-20 text-center shadow-sm border border-dashed border-gray-200">
            <div className="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-6">
              <Package size={40} className="text-gray-300" />
            </div>
            <h3 className="text-xl font-bold text-gray-800">Nenhum pedido encontrado</h3>
            <p className="text-gray-500 mt-2">Você ainda não tem pedidos nesta categoria.</p>
          </div>
        ) : (
          <div className="grid gap-6">
            {pedidosFiltrados.map((pedido) => {
              const config = getStatusConfig(pedido.status);
              return (
                <div 
                  key={pedido.id}
                  className="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-shadow group"
                >
                  <div className="p-8">
                    {/* Upper Row */}
                    <div className="flex flex-col md:flex-row justify-between gap-6 mb-8">
                      <div className="flex items-start gap-5">
                        <div className={`w-14 h-14 rounded-2xl flex items-center justify-center ${config.bg} ${config.text} border ${config.border} shrink-0`}>
                          {config.icon}
                        </div>
                        <div>
                          <div className="flex items-center gap-3 mb-1">
                            <span className="text-xs font-black uppercase tracking-widest text-gray-400">#{pedido.id.slice(-8)}</span>
                            <span className={`px-2 py-0.5 rounded-md text-[10px] font-black uppercase border ${config.bg} ${config.text} ${config.border}`}>
                              {config.label}
                            </span>
                          </div>
                          <h3 className="text-xl font-bold text-gray-900">
                            {pedido.itens.length === 1 
                              ? (produtos.find(p => p.id === pedido.itens[0].produtoId)?.nome || 'Produto')
                              : `${pedido.itens.length} Produtos`
                            }
                          </h3>
                          <p className="text-sm text-gray-400 mt-1 font-medium">
                            Realizado em {new Date(pedido.dataCriacao).toLocaleDateString('pt-BR')} às {new Date(pedido.dataCriacao).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
                          </p>
                        </div>
                      </div>
                      
                      <div className="flex flex-col md:items-end justify-center">
                        <span className="text-xs text-gray-400 font-bold mb-1">Total do Pedido</span>
                        <p className="text-3xl font-black text-petroleo-600">
                          R$ {pedido.total.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                        </p>
                      </div>
                    </div>

                    {/* Lower Row / Actions */}
                    <div className="pt-8 border-t border-gray-50 flex flex-col sm:flex-row items-center justify-between gap-4">
                      <div className="flex -space-x-3">
                        {pedido.itens.slice(0, 4).map((item, idx) => (
                           <div key={idx} className="w-10 h-10 rounded-full border-2 border-white bg-gray-100 flex items-center justify-center text-[10px] font-bold text-gray-600 overflow-hidden shadow-sm">
                             {produtos.find(p => p.id === item.produtoId)?.imagem ? (
                               <img src={produtos.find(p => p.id === item.produtoId)?.imagem} className="w-full h-full object-cover" alt="" />
                             ) : <Package size={14} />}
                           </div>
                        ))}
                        {pedido.itens.length > 4 && (
                          <div className="w-10 h-10 rounded-full border-2 border-white bg-gray-900 flex items-center justify-center text-[10px] font-bold text-white shadow-sm">
                            +{pedido.itens.length - 4}
                          </div>
                        )}
                      </div>

                      <div className="flex items-center gap-3 w-full sm:w-auto">
                        {pedido.status === 'RESERVED' && (
                          <>
                            <button
                              onClick={() => handleCancelarPedido(pedido.id)}
                              className="flex-1 sm:flex-none px-6 py-3 text-gray-400 font-bold hover:text-red-500 transition"
                            >
                              Cancelar
                            </button>
                            <button
                              onClick={() => handleAbrirPagamento(pedido)}
                              className="flex-1 sm:flex-none flex items-center justify-center gap-2 px-8 py-3 bg-petroleo-600 hover:bg-petroleo-700 text-white rounded-xl font-bold shadow-lg shadow-petroleo-600/20 transition-all"
                            >
                              Pagar Agora
                              <CreditCard size={18} />
                            </button>
                          </>
                        )}
                        
                        {pedido.status === 'PAID' && (
                          <button
                            onClick={() => gerarComprovante(pedido)}
                            className="w-full sm:w-auto flex items-center justify-center gap-2 px-6 py-3 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-xl font-bold transition-all"
                          >
                            <Download size={18} />
                            Comprovante PDF
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Modern Payment Modal */}
      {pagamentoModalAberto && pedidoSelecionado && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-petroleo-950/40 backdrop-blur-md" onClick={() => setPagamentoModalAberto(false)}></div>
          
          <div className="relative w-full max-w-lg bg-white rounded-[40px] shadow-2xl overflow-hidden animate-in zoom-in-95 duration-300">
            <div className="p-10">
              <div className="flex items-center justify-between mb-8">
                <div>
                  <h2 className="text-3xl font-black text-gray-900">Pagamento</h2>
                  <p className="text-gray-500 font-medium">Pedido #{pedidoSelecionado.id.slice(-8)}</p>
                </div>
                <button onClick={() => setPagamentoModalAberto(false)} className="p-3 hover:bg-gray-100 rounded-2xl transition">
                  <X size={24} className="text-gray-400" />
                </button>
              </div>

              <div className="space-y-6">
                <div className="grid gap-3">
                  <button
                    onClick={() => setMetodoPagamento('pix')}
                    className={`p-6 rounded-3xl border-2 transition-all flex items-center gap-5 ${
                      metodoPagamento === 'pix' ? 'border-cyan-600 bg-cyan-50 shadow-inner' : 'border-gray-50 hover:border-gray-100'
                    }`}
                  >
                    <div className="w-14 h-14 bg-cyan-100 rounded-2xl flex items-center justify-center shrink-0">
                      <TrendingUp className="text-cyan-600" size={28} />
                    </div>
                    <div className="text-left">
                      <p className="font-black text-gray-900 text-lg">PIX</p>
                      <p className="text-sm text-gray-500">Liberação instantânea</p>
                    </div>
                  </button>

                  <button
                    onClick={() => setMetodoPagamento('transferencia')}
                    className={`p-6 rounded-3xl border-2 transition-all flex items-center gap-5 ${
                      metodoPagamento === 'transferencia' ? 'border-petroleo-600 bg-petroleo-50 shadow-inner' : 'border-gray-50 hover:border-gray-100'
                    }`}
                  >
                    <div className="w-14 h-14 bg-petroleo-100 rounded-2xl flex items-center justify-center shrink-0">
                      <Wallet className="text-petroleo-600" size={28} />
                    </div>
                    <div className="text-left">
                      <p className="font-black text-gray-900 text-lg">Saldo em Conta</p>
                      <p className="text-sm text-gray-500">Usar saldo da carteira</p>
                    </div>
                  </button>
                </div>

                <div className="bg-gray-900 p-8 rounded-[32px] text-white">
                  <div className="flex justify-between items-center mb-2 opacity-50 text-sm font-bold">
                    <span>VALOR TOTAL</span>
                    <span>#{pedidoSelecionado.itens.length} Itens</span>
                  </div>
                  <div className="flex justify-between items-end">
                    <p className="text-4xl font-black text-cyan-400">
                      R$ {pedidoSelecionado.total.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                    </p>
                    <div className="flex items-center gap-2 bg-white/10 px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-widest border border-white/10">
                       <ShieldCheck size={12} className="text-cyan-400" />
                       Protegido
                    </div>
                  </div>
                </div>

                <button
                  onClick={handleConfirmarPagamento}
                  disabled={isProcessando || !metodoPagamento}
                  className="w-full py-6 bg-petroleo-600 hover:bg-petroleo-700 text-white rounded-3xl font-black text-xl shadow-xl shadow-petroleo-600/20 transition-all flex items-center justify-center gap-4 disabled:opacity-50"
                >
                  {isProcessando ? (
                    <div className="w-7 h-7 border-4 border-white/30 border-t-white rounded-full animate-spin"></div>
                  ) : (
                    <>
                      Confirmar e Pagar
                      <ArrowRight size={24} />
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
};
