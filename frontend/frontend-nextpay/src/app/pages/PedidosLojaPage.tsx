import React, { useState } from 'react';
import { Layout } from '../components/Layout';
import { useAuth } from '../contexts/AuthContext';
import { useMarketplace } from '../contexts/MarketplaceContext';
import {
  Package,
  CheckCircle,
  XCircle,
  Clock,
  Ban,
  Search,
} from 'lucide-react';
import { StatusPedido } from '../types';

export const PedidosLojaPage: React.FC = () => {
  const { user } = useAuth();
  const { getPedidosByVendedor, produtos } = useMarketplace();
  const [filtroStatus, setFiltroStatus] = useState<string>('todos');
  const [busca, setBusca] = useState('');

  const todosPedidos = getPedidosByVendedor(user?.id || '').sort((a, b) =>
    new Date(b.dataCriacao).getTime() - new Date(a.dataCriacao).getTime()
  );

  const pedidosFiltrados = todosPedidos.filter(pedido => {
    const matchStatus = filtroStatus === 'todos' || pedido.status === filtroStatus;
    const matchBusca = busca === '' || pedido.id.includes(busca);
    return matchStatus && matchBusca;
  });

  const getStatusIcon = (status: StatusPedido) => {
    switch (status) {
      case 'RESERVED':
        return <Clock className="text-yellow-600" size={24} />;
      case 'PAID':
        return <CheckCircle className="text-green-600" size={24} />;
      case 'FAILED':
        return <XCircle className="text-red-600" size={24} />;
      case 'CANCELED':
        return <Ban className="text-gray-600" size={24} />;
      case 'EXPIRED':
        return <Clock className="text-rose-600" size={24} />;
    }
  };

  const getStatusColor = (status: StatusPedido) => {
    switch (status) {
      case 'RESERVED':
        return 'bg-yellow-100';
      case 'PAID':
        return 'bg-green-100';
      case 'FAILED':
        return 'bg-red-100';
      case 'CANCELED':
        return 'bg-gray-100';
      case 'EXPIRED':
        return 'bg-rose-100';
    }
  };

  const getStatusBadgeColor = (status: StatusPedido) => {
    switch (status) {
      case 'RESERVED':
        return 'bg-yellow-100 text-yellow-700';
      case 'PAID':
        return 'bg-green-100 text-green-700';
      case 'FAILED':
        return 'bg-red-100 text-red-700';
      case 'CANCELED':
        return 'bg-gray-100 text-gray-700';
      case 'EXPIRED':
        return 'bg-rose-100 text-rose-700';
    }
  };

  const getStatusLabel = (status: StatusPedido) => {
    switch (status) {
      case 'RESERVED':
        return 'Reservado';
      case 'PAID':
        return 'Pago';
      case 'FAILED':
        return 'Falhou';
      case 'CANCELED':
        return 'Cancelado';
      case 'EXPIRED':
        return 'Expirado';
    }
  };

  const totalPedidos = todosPedidos.length;
  const pedidosReservados = todosPedidos.filter(p => p.status === 'RESERVED').length;
  const pedidosPagos = todosPedidos.filter(p => p.status === 'PAID').length;
  const faturamentoTotal = todosPedidos
    .filter(p => p.status === 'PAID')
    .reduce((sum, p) => sum + p.total, 0);

  return (
    <Layout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-800">Gerenciar Pedidos</h1>
          <p className="text-gray-600 mt-1">Visualize e acompanhe todos os pedidos da loja</p>
        </div>

        {/* Estatísticas */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-white p-6 rounded-xl shadow-md">
            <p className="text-sm text-gray-600">Total de Pedidos</p>
            <p className="text-3xl font-bold text-gray-800 mt-2">{totalPedidos}</p>
          </div>
          <div className="bg-white p-6 rounded-xl shadow-md">
            <p className="text-sm text-gray-600">Reservados</p>
            <p className="text-3xl font-bold text-yellow-600 mt-2">{pedidosReservados}</p>
          </div>
          <div className="bg-white p-6 rounded-xl shadow-md">
            <p className="text-sm text-gray-600">Pagos</p>
            <p className="text-3xl font-bold text-green-600 mt-2">{pedidosPagos}</p>
          </div>
          <div className="bg-white p-6 rounded-xl shadow-md">
            <p className="text-sm text-gray-600">Faturamento Total</p>
            <p className="text-3xl font-bold text-blue-600 mt-2">
              R$ {faturamentoTotal.toFixed(2)}
            </p>
          </div>
        </div>

        {/* Filtros */}
        <div className="bg-white p-6 rounded-xl shadow-md">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
              <input
                type="text"
                value={busca}
                onChange={(e) => setBusca(e.target.value)}
                className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                placeholder="Buscar por ID do pedido..."
              />
            </div>
            <select
              value={filtroStatus}
              onChange={(e) => setFiltroStatus(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
            >
              <option value="todos">Todos os Status</option>
              <option value="RESERVED">Reservados</option>
              <option value="PAID">Pagos</option>
              <option value="FAILED">Falhou</option>
              <option value="CANCELED">Cancelados</option>
              <option value="EXPIRED">Expirados</option>
            </select>
          </div>
        </div>

        {/* Lista de Pedidos */}
        {pedidosFiltrados.length === 0 ? (
          <div className="bg-white rounded-xl shadow-md p-12 text-center">
            <Package className="mx-auto text-gray-400 mb-4" size={64} />
            <h3 className="text-xl font-semibold text-gray-800 mb-2">Nenhum pedido encontrado</h3>
            <p className="text-gray-600">Ajuste os filtros ou aguarde novos pedidos</p>
          </div>
        ) : (
          <div className="space-y-4">
            {pedidosFiltrados.map((pedido) => (
              <div key={pedido.id} className="bg-white rounded-xl shadow-md overflow-hidden">
                <div className="p-6">
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex items-center gap-4">
                      <div className={`w-12 h-12 rounded-full flex items-center justify-center ${getStatusColor(pedido.status)}`}>
                        {getStatusIcon(pedido.status)}
                      </div>
                      <div>
                        <h3 className="font-semibold text-gray-800">
                          Pedido #{pedido.id.slice(-8)}
                        </h3>
                        <p className="text-sm text-gray-600">
                          Cliente ID: {pedido.clienteId.slice(-8)}
                        </p>
                        <p className="text-sm text-gray-600">
                          {new Date(pedido.dataCriacao).toLocaleString('pt-BR')}
                        </p>
                      </div>
                    </div>
                    <div className="text-right">
                      <span className={`inline-block px-3 py-1 rounded-full text-xs font-semibold ${getStatusBadgeColor(pedido.status)}`}>
                        {getStatusLabel(pedido.status)}
                      </span>
                      <p className="text-2xl font-bold text-gray-800 mt-2">
                        R$ {pedido.total.toFixed(2)}
                      </p>
                    </div>
                  </div>

                  <div className="border-t pt-4">
                    <h4 className="font-semibold text-gray-800 mb-3">Itens do Pedido:</h4>
                    <div className="space-y-2">
                      {pedido.itens.map((item, index) => {
                        const produto = produtos.find(p => p.id === item.produtoId);
                        return (
                          <div key={index} className="flex items-center justify-between text-sm bg-gray-50 p-3 rounded">
                            <span className="text-gray-700">
                              {produto?.nome || 'Produto não encontrado'} x{item.quantidade}
                            </span>
                            <span className="text-gray-900 font-semibold">
                              R$ {(item.precoUnitario * item.quantidade).toFixed(2)}
                            </span>
                          </div>
                        );
                      })}
                    </div>
                  </div>

                  {pedido.dataPagamento && (
                    <div className="mt-4 text-sm text-gray-600">
                      Pago em: {new Date(pedido.dataPagamento).toLocaleString('pt-BR')}
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
};
