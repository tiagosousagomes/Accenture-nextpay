import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useBank } from '../contexts/BankContext';
import { useMarketplace } from '../contexts/MarketplaceContext';
import { Layout } from '../components/Layout';
import {
  ArrowUpCircle,
  ArrowDownCircle,
  ShoppingBag,
  TrendingUp,
  Wallet,
} from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

export const DashboardPage: React.FC = () => {
  const { user, isShopMode } = useAuth();
  const { conta, transacoes } = useBank();
  const { getPedidosByCliente, getAllPedidos, getProdutosByVendedor } = useMarketplace();
  const navigate = useNavigate();

  const pedidos = isShopMode
    ? getAllPedidos()
    : getPedidosByCliente(user?.id || '');

  const pedidosPagos = pedidos.filter(p => p.status === 'PAID');

    // Dados para o gráfico
  const isEntradaTransacao = (tipo: string, descricao?: string) => {
    return (
      tipo === 'deposito' ||
      tipo === 'venda' ||
      tipo === 'transferencia_recebida' ||
      tipo === 'pix_recebido' ||
      descricao?.toLowerCase().includes('recebido') ||
      descricao?.toLowerCase().includes('venda')
    );
  };

  const isSaidaTransacao = (tipo: string, descricao?: string) => {
    return (
      tipo === 'saque' ||
      tipo === 'compra' ||
      tipo === 'transferencia_enviada' ||
      tipo === 'pix_enviado' ||
      tipo === 'pagamento_pedido' ||
      tipo === 'pix' ||
      tipo === 'transferencia' ||
      descricao?.toLowerCase().includes('enviado') ||
      descricao?.toLowerCase().includes('compra')
    );
  };

  const totalEntradas = transacoes
    .filter(t => isEntradaTransacao(t.tipo, t.descricao))
    .reduce((acc, t) => acc + t.valor, 0);

  const totalSaidas = transacoes
    .filter(t => isSaidaTransacao(t.tipo, t.descricao))
    .reduce((acc, t) => acc + t.valor, 0);

  const transacoesOrdenadas = [...transacoes].reverse();

let saldoAcumulado = 0;

const dadosGrafico = [
  {
    data: 'Início',
    saldo: 0,
  },
  ...transacoesOrdenadas.map((t) => {
    const entrada = isEntradaTransacao(t.tipo, t.descricao);

    saldoAcumulado = entrada
      ? saldoAcumulado + t.valor
      : saldoAcumulado - t.valor;

    return {
      data: new Date(t.data).toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
      }),
      saldo: saldoAcumulado,
    };
  }),
];

  return (
    <Layout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-800">
            Bem-vindo, {user?.nome}!
          </h1>
          <p className="text-gray-600 mt-1">
            {isShopMode ? 'Gerencie sua loja e finanças' : 'Acompanhe suas finanças'}
          </p>
        </div>

        {/* Cards de Resumo */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div className="bg-white p-6 rounded-xl shadow-md">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Saldo Disponível</p>
                <p className="text-2xl font-bold text-gray-800 mt-1">
                  R$ {conta?.saldo.toFixed(2) || '0.00'}
                </p>
              </div>
              <div className="w-12 h-12 bg-petroleo-100 rounded-full flex items-center justify-center">
                <Wallet className="text-petroleo-700" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-xl shadow-md">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Total Entradas</p>
                <p className="text-2xl font-bold text-green-600 mt-1">
                  R$ {totalEntradas.toFixed(2)}
                </p>
              </div>
              <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
                <ArrowDownCircle className="text-green-600" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-xl shadow-md">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Total Saídas</p>
                <p className="text-2xl font-bold text-red-600 mt-1">
                  R$ {totalSaidas.toFixed(2)}
                </p>
              </div>
              <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center">
                <ArrowUpCircle className="text-red-600" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-xl shadow-md">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">
                  {isShopMode ? 'Vendas Realizadas' : 'Compras Realizadas'}
                </p>
                <p className="text-2xl font-bold text-petroleo-700 mt-1">
                  {pedidosPagos.length}
                </p>
              </div>
              <div className="w-12 h-12 bg-petroleo-100 rounded-full flex items-center justify-center">
                <ShoppingBag className="text-petroleo-700" size={24} />
              </div>
            </div>
          </div>
        </div>

        {/* Gráfico de Saldo */}
        {dadosGrafico.length > 0 && (
          <div className="bg-white p-6 rounded-xl shadow-md">
            <div className="flex items-center gap-2 mb-6">
              <TrendingUp className="text-petroleo-700" size={24} />
              <h2 className="text-xl font-semibold text-gray-800">Evolução do Saldo</h2>
            </div>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={dadosGrafico}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="data" />
                <YAxis />
                <Tooltip
                  formatter={(value: number) => `R$ ${value.toFixed(2)}`}
                />
                <Line
                  type="monotone"
                  dataKey="saldo"
                  stroke="#005f73"
                  strokeWidth={2}
                  dot={{ fill: '#005f73', r: 4 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        )}

        {/* Transações Recentes */}
        <div className="bg-white p-6 rounded-xl shadow-md">
          <h2 className="text-xl font-semibold text-gray-800 mb-4">Transações Recentes</h2>
          <div className="space-y-3">
            {transacoes.slice(0, 5).map((transacao) => {
              const isEntrada = isEntradaTransacao(transacao.tipo, transacao.descricao);
                transacao.tipo === 'transferencia_recebida' ||
                transacao.tipo === 'pix_recebido';

              return (
                <div
                  key={transacao.id}
                  className="flex items-center justify-between p-4 bg-gray-50 rounded-lg"
                >
                  <div className="flex items-center gap-3">
                    <div className={`w-10 h-10 rounded-full flex items-center justify-center ${isEntrada ? 'bg-green-100' : 'bg-red-100'
                      }`}>
                      {isEntrada ? (
                        <ArrowDownCircle className="text-green-600" size={20} />
                      ) : (
                        <ArrowUpCircle className="text-red-600" size={20} />
                      )}
                    </div>
                    <div>
                      <p className="font-medium text-gray-800">{transacao.descricao}</p>
                      <p className="text-sm text-gray-500">
                        {new Date(transacao.data).toLocaleString('pt-BR')}
                      </p>
                    </div>
                  </div>
                  <p className={`font-semibold ${isEntrada ? 'text-green-600' : 'text-red-600'}`}>
                    {isEntrada ? '+' : '-'} R$ {transacao.valor.toFixed(2)}
                  </p>
                </div>
              );
            })}

            {transacoes.length === 0 && (
              <p className="text-center text-gray-500 py-8">Nenhuma transação ainda</p>
            )}
          </div>
        </div>

        {/* Banner para não vendedores */}
        {!isShopMode && getProdutosByVendedor(user?.id || '').length === 0 && (
          <div className="bg-gradient-to-r from-petroleo-800 to-petroleo-600 rounded-xl p-8 text-white shadow-lg flex flex-col md:flex-row items-center justify-between gap-6">
            <div className="flex-1">
              <h3 className="text-2xl font-bold mb-2 flex items-center gap-2">
                🚀 Não é vendedor?
              </h3>
              <p className="text-petroleo-100 text-lg">
                Cadastre seu primeiro produto e transforme sua conta em uma nova oportunidade de negócio!
              </p>
            </div>
            <button
              onClick={() => navigate('/produtos')}
              className="px-8 py-3 bg-white text-petroleo-900 rounded-lg font-bold hover:bg-petroleo-50 transition transform hover:scale-105 shadow-md"
            >
              Começar a Vender
            </button>
          </div>
        )}
      </div>
    </Layout>
  );
};
