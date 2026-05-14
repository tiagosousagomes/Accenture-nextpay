import React, { useState } from 'react';
import { Layout } from '../components/Layout';
import { useMarketplace } from '../contexts/MarketplaceContext';
import { useBank } from '../contexts/BankContext';
import { useRewards } from '../contexts/RewardsContext';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import {
  ShoppingCart,
  Plus,
  Minus,
  Trash2,
  Package,
  AlertCircle,
  X,
  CheckCircle,
  Store,
  TrendingUp,
  Wallet,
  ArrowRight,
  ShieldCheck,
  CreditCard,
} from 'lucide-react';
import { toast } from 'sonner';

export const MarketplacePage: React.FC = () => {
  const { user } = useAuth();
  const {
    produtos,
    carrinho,
    adicionarAoCarrinho,
    removerDoCarrinho,
    atualizarQuantidade,
    confirmarPagamento,
    lojaAberta,
  } = useMarketplace();
  const { conta, refreshConta } = useBank();
  const { adicionarMoedas, adicionarTransacao, calcularRecompensaCompra } = useRewards();
  const navigate = useNavigate();
  
  const [carrinhoAberto, setCarrinhoAberto] = useState(false);
  const [passoPagamento, setPassoPagamento] = useState<'carrinho' | 'pagamento' | 'sucesso'>('carrinho');
  const [metodoPagamento, setMetodoPagamento] = useState<'pix' | 'transferencia' | null>(null);
  const [isProcessando, setIsProcessando] = useState(false);

  const totalCarrinho = carrinho.reduce(
    (sum, item) => sum + item.produto.preco * item.quantidade,
    0
  );

  const handleAdicionarAoCarrinho = async (produto: any) => {
    if (!lojaAberta) {
      toast.error('A loja está fechada no momento');
      return;
    }

    if (produto.estoque <= 0) {
      toast.error('Produto sem estoque');
      return;
    }

    setIsProcessando(true);
    try {
      const sucesso = await adicionarAoCarrinho(produto, 1);
      if (sucesso) {
        toast.success(
          <div className="flex flex-col">
            <span className="font-bold">Produto Reservado!</span>
            <span className="text-xs">O item foi bloqueado para você por tempo limitado.</span>
          </div>
        );
      } else {
        toast.error('Erro ao reservar produto');
      }
    } finally {
      setIsProcessando(false);
    }
  };

  const handleFinalizarCompra = async () => {
    if (!metodoPagamento) {
      toast.error('Selecione uma forma de pagamento');
      return;
    }

    if (!conta || totalCarrinho > conta.saldo) {
      toast.error('Saldo insuficiente');
      return;
    }

    setIsProcessando(true);
    try {
      const sucesso = await confirmarPagamento(carrinho);

      if (sucesso) {
        // Recompensas
        const moedas = calcularRecompensaCompra(totalCarrinho);
        adicionarTransacao('compra', totalCarrinho);
        adicionarMoedas(moedas);
        refreshConta();

        setPassoPagamento('sucesso');
        setTimeout(() => {
          setCarrinhoAberto(false);
          setPassoPagamento('carrinho');
          setMetodoPagamento(null);
          navigate('/pedidos');
        }, 3000);
      } else {
        toast.error('Erro ao processar pagamento. Tente novamente em "Meus Pedidos".');
        navigate('/pedidos');
      }
    } catch (err) {
      toast.error('Erro inesperado ao processar pagamento');
    } finally {
      setIsProcessando(false);
    }
  };

  return (
    <Layout>
      <div className="max-w-7xl mx-auto space-y-10">
        {/* Hero Section */}
        <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-petroleo-900 via-petroleo-800 to-cyan-900 p-12 text-white shadow-2xl">
          <div className="relative z-10 max-w-2xl">
            <h1 className="text-5xl font-extrabold tracking-tight mb-4">
              Explore o <span className="text-cyan-400">Marketplace</span>
            </h1>
            <p className="text-cyan-100 text-xl mb-8 leading-relaxed">
              Descubra produtos incríveis e aproveite benefícios exclusivos. Ao adicionar ao carrinho, sua reserva é garantida instantaneamente.
            </p>
            <div className="flex gap-4">
              <div className="flex items-center gap-2 bg-white/10 backdrop-blur-md px-4 py-2 rounded-full border border-white/20">
                <ShieldCheck size={18} className="text-cyan-400" />
                <span className="text-sm font-medium">Compra Protegida</span>
              </div>
              <div className="flex items-center gap-2 bg-white/10 backdrop-blur-md px-4 py-2 rounded-full border border-white/20">
                <TrendingUp size={18} className="text-cyan-400" />
                <span className="text-sm font-medium">Cashback em Moedas</span>
              </div>
            </div>
          </div>
          
          {/* Abstract Decorations */}
          <div className="absolute top-0 right-0 -translate-y-1/2 translate-x-1/4 w-96 h-96 bg-cyan-500/20 rounded-full blur-[100px]"></div>
          <div className="absolute bottom-0 left-0 translate-y-1/2 -translate-x-1/4 w-96 h-96 bg-purple-500/10 rounded-full blur-[100px]"></div>
        </div>

        {/* Info Alerts */}
        {!lojaAberta && (
          <div className="bg-amber-50 border border-amber-200 rounded-2xl p-6 flex items-center gap-4 text-amber-800 shadow-sm animate-pulse">
            <div className="w-12 h-12 bg-amber-100 rounded-xl flex items-center justify-center flex-shrink-0">
              <AlertCircle size={24} />
            </div>
            <div>
              <p className="font-bold text-lg">Marketplace em Manutenção</p>
              <p className="opacity-90">A loja está temporariamente fechada para novas reservas. Volte em breve!</p>
            </div>
          </div>
        )}

        {produtos.filter((p) => p.vendedorId === user?.id).length === 0 && (
          <div className="bg-gradient-to-r from-cyan-700 to-cyan-500 rounded-xl px-6 py-4 text-white shadow-md flex flex-col md:flex-row items-center justify-between gap-4 mb-6">
            <div>
              <h3 className="text-xl font-bold flex items-center gap-2">
                🚀 Não é vendedor?
              </h3>

              <p className="text-cyan-50 text-sm">
                Cadastre seu primeiro produto e transforme sua conta em uma nova oportunidade de negócio!
              </p>
            </div>

            <button
              onClick={() => navigate('/produtos')}
              className="px-6 py-2 bg-white text-cyan-800 rounded-lg font-bold hover:bg-cyan-50 transition shadow-md"
            >
              Começar a Vender
            </button>
          </div>
        )}

        {/* Search & Actions Bar */}
        <div className="flex flex-col md:flex-row gap-4 items-center justify-between bg-white p-4 rounded-2xl shadow-sm border border-gray-100">
          <div className="flex items-center gap-4 w-full md:w-auto">
             <div className="h-10 w-1 bg-petroleo-600 rounded-full hidden md:block"></div>
             <h2 className="text-xl font-bold text-gray-800">Produtos Disponíveis</h2>
          </div>
          
          <button
            onClick={() => setCarrinhoAberto(true)}
            className="w-full md:w-auto relative group flex items-center gap-3 px-8 py-4 bg-petroleo-600 hover:bg-petroleo-700 text-white rounded-xl font-bold transition-all shadow-lg hover:shadow-petroleo-600/20"
          >
            <ShoppingCart size={20} className="group-hover:scale-110 transition" />
            Meu Carrinho
            <span className="ml-2 bg-white text-petroleo-600 px-2 py-0.5 rounded-full text-xs font-black">
              {carrinho.length}
            </span>
          </button>
        </div>
        

        {/* Product Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
          {produtos.filter(p => p.vendedorId !== user?.id).map((produto) => (
            <div
              key={produto.id}
              className="group bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-xl hover:-translate-y-1 transition-all duration-300"
            >
              {/* Product Image Container */}
              <div className="relative h-64 bg-gray-50 overflow-hidden">
                {produto.imagem ? (
                  <img 
                    src={produto.imagem} 
                    alt={produto.nome} 
                    className="w-full h-full object-cover group-hover:scale-110 transition duration-500" 
                  />
                ) : (
                  <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-gray-50 to-gray-100">
                    <Package className="text-gray-200" size={80} />
                  </div>
                )}
                
                {/* Seller Badge */}
                <div className="absolute top-4 left-4 flex items-center gap-2 bg-white/90 backdrop-blur-md px-3 py-1.5 rounded-full shadow-sm border border-white/50">
                   <div className="w-5 h-5 rounded-full bg-petroleo-100 flex items-center justify-center overflow-hidden">
                     <Store size={12} className="text-petroleo-600" />
                   </div>
                   <span className="text-xs font-bold text-gray-700">{produto.vendedorNome || 'Vendedor'}</span>
                </div>

                {/* Status Overlay */}
                {produto.estoque <= 5 && produto.estoque > 0 && (
                   <div className="absolute bottom-4 right-4 bg-orange-500 text-white text-[10px] font-black uppercase tracking-wider px-2 py-1 rounded">
                     Últimas Unidades
                   </div>
                )}
              </div>

              {/* Product Details */}
              <div className="p-6 space-y-4">
                <div>
                  <h3 className="font-bold text-gray-900 text-lg group-hover:text-petroleo-600 transition truncate">
                    {produto.nome}
                  </h3>
                  <p className="text-sm text-gray-500 line-clamp-1">{produto.descricao || 'Sem descrição disponível'}</p>
                </div>

                <div className="flex items-end justify-between">
                  <div className="space-y-1">
                    <span className="text-xs text-gray-400 font-medium">Preço à vista</span>
                    <p className="text-2xl font-black text-petroleo-600">
                      R$ {produto.preco.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                    </p>
                  </div>
                  <div className="text-right">
                    <span className={`text-xs font-bold ${produto.estoque > 0 ? 'text-green-600' : 'text-red-500'}`}>
                      {produto.estoque > 0 ? `${produto.estoque} em estoque` : 'Esgotado'}
                    </span>
                  </div>
                </div>

                <button
                  onClick={() => handleAdicionarAoCarrinho(produto)}
                  disabled={produto.estoque <= 0 || !lojaAberta || isProcessando}
                  className={`w-full group/btn flex items-center justify-center gap-2 py-4 rounded-2xl font-bold transition-all ${
                    produto.estoque > 0 && lojaAberta
                    ? 'bg-gray-900 text-white hover:bg-petroleo-600 shadow-md'
                    : 'bg-gray-100 text-gray-400 cursor-not-allowed'
                  }`}
                >
                  {isProcessando ? (
                    <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                  ) : (
                    <>
                      {produto.estoque > 0 ? (
                        <>
                          Reservar Agora
                          <ArrowRight size={18} className="group-hover/btn:translate-x-1 transition" />
                        </>
                      ) : 'Indisponível'}
                    </>
                  )}
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Modern Cart Sidebar */}
      {carrinhoAberto && (
        <div className="fixed inset-0 z-50 flex justify-end">
          {/* Backdrop */}
          <div 
            className="absolute inset-0 bg-petroleo-950/60 backdrop-blur-sm transition-opacity"
            onClick={() => setCarrinhoAberto(false)}
          ></div>
          
          {/* Sidebar */}
          <div className="relative w-full max-w-md bg-white h-full shadow-2xl flex flex-col animate-in slide-in-from-right duration-300">
            {/* Header */}
            <div className="p-8 border-b flex items-center justify-between">
              <div>
                <h2 className="text-2xl font-black text-gray-900">Seu Carrinho</h2>
                <p className="text-sm text-gray-500">
                  {passoPagamento === 'carrinho' ? 'Revise suas reservas' : 'Finalizar pagamento'}
                </p>
              </div>
              <button
                onClick={() => setCarrinhoAberto(false)}
                className="p-3 hover:bg-gray-100 rounded-2xl transition"
              >
                <X size={24} className="text-gray-400" />
              </button>
            </div>

            {/* Content Area */}
            <div className="flex-1 overflow-y-auto p-8">
              {passoPagamento === 'carrinho' ? (
                carrinho.length === 0 ? (
                  <div className="h-full flex flex-col items-center justify-center text-center space-y-4">
                    <div className="w-24 h-24 bg-gray-50 rounded-full flex items-center justify-center">
                      <ShoppingCart className="text-gray-200" size={48} />
                    </div>
                    <p className="text-gray-500 font-medium">Seu carrinho está vazio</p>
                    <button 
                      onClick={() => setCarrinhoAberto(false)}
                      className="text-petroleo-600 font-bold hover:underline"
                    >
                      Explorar produtos
                    </button>
                  </div>
                ) : (
                  <div className="space-y-6">
                    {carrinho.map((item) => (
                      <div
                        key={item.produto.id}
                        className="flex items-center gap-4 group"
                      >
                        <div className="w-20 h-20 bg-gray-50 rounded-2xl flex-shrink-0 overflow-hidden border border-gray-100">
                          {item.produto.imagem ? (
                            <img src={item.produto.imagem} alt={item.produto.nome} className="w-full h-full object-cover" />
                          ) : (
                            <Package className="w-full h-full p-4 text-gray-200" />
                          )}
                        </div>

                        <div className="flex-1">
                          <h3 className="font-bold text-gray-900 leading-tight">{item.produto.nome}</h3>
                          <p className="text-sm text-petroleo-600 font-black">
                            R$ {item.produto.preco.toLocaleString('pt-BR')}
                          </p>
                          
                          <div className="mt-2 flex items-center gap-3">
                            <div className="flex items-center gap-1 bg-gray-50 p-1 rounded-lg">
                              <button
                                onClick={() => atualizarQuantidade(item.produto.id, item.quantidade - 1)}
                                className="w-7 h-7 flex items-center justify-center hover:bg-white hover:shadow-sm rounded-md transition"
                              >
                                <Minus size={14} />
                              </button>
                              <span className="w-6 text-center text-xs font-black">{item.quantidade}</span>
                              <button
                                onClick={() => atualizarQuantidade(item.produto.id, item.quantidade + 1)}
                                className="w-7 h-7 flex items-center justify-center hover:bg-white hover:shadow-sm rounded-md transition"
                              >
                                <Plus size={14} />
                              </button>
                            </div>
                            <button
                              onClick={() => removerDoCarrinho(item.produto.id)}
                              className="text-red-400 hover:text-red-600 p-1 transition"
                            >
                              <Trash2 size={16} />
                            </button>
                          </div>
                        </div>
                      </div>
                    ))}
                    
                    {/* Reservation Badge */}
                    <div className="bg-cyan-50 border border-cyan-100 p-4 rounded-2xl flex items-start gap-3">
                      <ShieldCheck className="text-cyan-600 shrink-0" size={20} />
                      <p className="text-xs text-cyan-800 font-medium leading-relaxed">
                        Seus itens foram <span className="font-bold">reservados no servidor</span>. Você tem 30 minutos para concluir o pagamento antes que eles voltem ao estoque.
                      </p>
                    </div>
                  </div>
                )
              ) : passoPagamento === 'pagamento' ? (
                <div className="space-y-8">
                  <div className="space-y-4">
                    <h3 className="font-bold text-gray-900">Método de Pagamento</h3>
                    <div className="grid gap-3">
                      <button
                        onClick={() => setMetodoPagamento('pix')}
                        className={`p-5 rounded-2xl border-2 transition-all flex items-center gap-4 ${
                          metodoPagamento === 'pix' 
                          ? 'border-cyan-600 bg-cyan-50 shadow-sm' 
                          : 'border-gray-100 hover:border-gray-200'
                        }`}
                      >
                        <div className="w-12 h-12 bg-cyan-100 rounded-xl flex items-center justify-center">
                          <TrendingUp className="text-cyan-600" size={24} />
                        </div>
                        <div className="text-left">
                          <p className="font-bold text-gray-900">PIX Instantâneo</p>
                          <p className="text-xs text-gray-500">Aprovação imediata</p>
                        </div>
                      </button>

                      <button
                        onClick={() => setMetodoPagamento('transferencia')}
                        className={`p-5 rounded-2xl border-2 transition-all flex items-center gap-4 ${
                          metodoPagamento === 'transferencia' 
                          ? 'border-petroleo-600 bg-petroleo-50 shadow-sm' 
                          : 'border-gray-100 hover:border-gray-200'
                        }`}
                      >
                        <div className="w-12 h-12 bg-petroleo-100 rounded-xl flex items-center justify-center">
                          <Wallet className="text-petroleo-600" size={24} />
                        </div>
                        <div className="text-left">
                          <p className="font-bold text-gray-900">Saldo MarketPay</p>
                          <p className="text-xs text-gray-500">Transferência entre contas</p>
                        </div>
                      </button>
                    </div>
                  </div>

                  <div className="bg-gray-900 p-6 rounded-3xl text-white space-y-4">
                     <div className="flex justify-between items-center opacity-60 text-sm">
                       <span>Subtotal</span>
                       <span>R$ {totalCarrinho.toLocaleString('pt-BR')}</span>
                     </div>
                     <div className="flex justify-between items-center">
                       <span className="font-bold">Total a pagar</span>
                       <span className="text-2xl font-black text-cyan-400">R$ {totalCarrinho.toLocaleString('pt-BR')}</span>
                     </div>
                  </div>
                </div>
              ) : (
                <div className="h-full flex flex-col items-center justify-center text-center space-y-6">
                  <div className="w-24 h-24 bg-green-100 rounded-full flex items-center justify-center text-green-600 animate-bounce">
                    <CheckCircle size={48} />
                  </div>
                  <div>
                    <h3 className="text-2xl font-black text-gray-900">Pagamento Confirmado!</h3>
                    <p className="text-gray-500 mt-2">Sua reserva foi finalizada com sucesso. Redirecionando...</p>
                  </div>
                </div>
              )}
            </div>

            {/* Footer */}
            {carrinho.length > 0 && passoPagamento !== 'sucesso' && (
              <div className="p-8 bg-gray-50 border-t">
                {passoPagamento === 'carrinho' ? (
                  <button
                    onClick={() => setPassoPagamento('pagamento')}
                    className="w-full py-5 bg-petroleo-600 hover:bg-petroleo-700 text-white rounded-2xl font-black text-lg shadow-xl shadow-petroleo-600/20 flex items-center justify-center gap-3 transition-all"
                  >
                    Próximo Passo
                    <ArrowRight size={22} />
                  </button>
                ) : (
                  <div className="flex flex-col gap-3">
                    <button
                      onClick={handleFinalizarCompra}
                      disabled={isProcessando || !metodoPagamento}
                      className="w-full py-5 bg-cyan-600 hover:bg-cyan-700 text-white rounded-2xl font-black text-lg shadow-xl shadow-cyan-600/20 flex items-center justify-center gap-3 transition-all disabled:opacity-50"
                    >
                      {isProcessando ? (
                        <div className="w-6 h-6 border-3 border-white/30 border-t-white rounded-full animate-spin"></div>
                      ) : (
                        <>
                          <CreditCard size={22} />
                          Finalizar Pagamento
                        </>
                      )}
                    </button>
                    <button
                      onClick={() => setPassoPagamento('carrinho')}
                      disabled={isProcessando}
                      className="w-full py-3 text-gray-500 font-bold hover:text-gray-700 transition"
                    >
                      Voltar ao carrinho
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      )}
    </Layout>
  );
};
