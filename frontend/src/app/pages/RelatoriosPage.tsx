import React from 'react';
import { Layout } from '../components/Layout';
import { useMarketplace } from '../contexts/MarketplaceContext';
import {
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { TrendingUp, Package, Users, DollarSign, Printer } from 'lucide-react';
import { jsPDF } from 'jspdf';
import { toast } from 'sonner';

import { useAuth } from '../contexts/AuthContext';

export const RelatoriosPage: React.FC = () => {
  const { user } = useAuth();
  const { getPedidosByVendedor, produtos } = useMarketplace();

  const imprimirRelatorio = () => {
    const doc = new jsPDF();

    // Cabeçalho
    doc.setFontSize(20);
    doc.text('MarketPay - Relatorio de Vendas', 20, 20);

    doc.setFontSize(12);
    doc.text(`Data: ${new Date().toLocaleDateString('pt-BR')}`, 20, 35);
    doc.text(`Total de Vendas: ${pedidosPagos.length}`, 20, 42);
    doc.text(`Faturamento Total: R$ ${faturamentoTotal.toFixed(2)}`, 20, 49);

    doc.line(20, 54, 190, 54);

    // Produtos Mais Vendidos
    doc.setFontSize(14);
    doc.text('Top 5 Produtos Mais Vendidos', 20, 64);

    doc.setFontSize(10);
    let y = 74;

    produtosMaisVendidos.forEach((produto, index) => {
      if (y > 270) {
        doc.addPage();
        y = 20;
      }

      doc.text(`${index + 1}. ${produto.nome}`, 20, y);
      doc.text(`${produto.quantidade} unidades`, 20, y + 5);
      doc.text(`R$ ${produto.valor.toFixed(2)}`, 160, y);

      y += 15;
    });

    // Nova página para categorias
    doc.addPage();
    y = 20;

    doc.setFontSize(14);
    doc.text('Vendas por Categoria', 20, y);
    y += 10;

    doc.setFontSize(10);
    dadosCategoria.forEach((cat) => {
      if (y > 270) {
        doc.addPage();
        y = 20;
      }

      doc.text(cat.categoria, 20, y);
      doc.text(`R$ ${cat.valor.toFixed(2)}`, 160, y);
      y += 10;
    });

    // Top Clientes
    doc.addPage();
    y = 20;

    doc.setFontSize(14);
    doc.text('Top 5 Clientes', 20, y);
    y += 10;

    doc.setFontSize(10);
    topClientes.forEach((cliente, index) => {
      if (y > 270) {
        doc.addPage();
        y = 20;
      }

      doc.text(`${index + 1}. Cliente #${cliente.id}`, 20, y);
      doc.text(`${cliente.compras} compras`, 20, y + 5);
      doc.text(`R$ ${cliente.total.toFixed(2)}`, 160, y);
      y += 15;
    });

    doc.save(`relatorio-vendas-${new Date().getTime()}.pdf`);
    toast.success('Relatório baixado com sucesso!');
  };

  const pedidosPagos = getPedidosByVendedor(user?.id || '').filter(p => p.status === 'PAID');

  // Faturamento Total
  const faturamentoTotal = pedidosPagos.reduce((sum, p) => sum + p.total, 0);

  // Produtos Mais Vendidos
  const vendasPorProduto = new Map<string, { quantidade: number; valor: number }>();

  pedidosPagos.forEach(pedido => {
    pedido.itens.forEach(item => {
      const current = vendasPorProduto.get(item.produtoId) || { quantidade: 0, valor: 0 };
      vendasPorProduto.set(item.produtoId, {
        quantidade: current.quantidade + item.quantidade,
        valor: current.valor + (item.precoUnitario * item.quantidade),
      });
    });
  });

  const produtosMaisVendidos = Array.from(vendasPorProduto.entries())
    .map(([id, stats]) => {
      const produto = produtos.find(p => p.id === id);
      return {
        nome: produto?.nome || 'Produto Removido',
        quantidade: stats.quantidade,
        valor: stats.valor,
      };
    })
    .sort((a, b) => b.quantidade - a.quantidade)
    .slice(0, 5);

  // Vendas por Categoria
  const vendasPorCategoria = new Map<string, number>();

  pedidosPagos.forEach(pedido => {
    pedido.itens.forEach(item => {
      const produto = produtos.find(p => p.id === item.produtoId);
      if (produto) {
        const current = vendasPorCategoria.get(produto.categoria) || 0;
        vendasPorCategoria.set(produto.categoria, current + (item.precoUnitario * item.quantidade));
      }
    });
  });

  const dadosCategoria = Array.from(vendasPorCategoria.entries()).map(([categoria, valor]) => ({
    categoria,
    valor,
  }));

  // Clientes com Mais Compras
  const comprasPorCliente = new Map<string, { quantidade: number; total: number }>();

  pedidosPagos.forEach(pedido => {
    const current = comprasPorCliente.get(pedido.clienteId) || { quantidade: 0, total: 0 };
    comprasPorCliente.set(pedido.clienteId, {
      quantidade: current.quantidade + 1,
      total: current.total + pedido.total,
    });
  });

  const topClientes = Array.from(comprasPorCliente.entries())
    .map(([id, stats]) => ({
      id: id.slice(-8),
      compras: stats.quantidade,
      total: stats.total,
    }))
    .sort((a, b) => b.total - a.total)
    .slice(0, 5);

  const COLORS = ['#005f73', '#0a9396', '#94d2bd', '#e9d8a6', '#ee9b00'];

  return (
    <Layout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-800">Relatórios e Análises</h1>
            <p className="text-gray-600 mt-1">Insights sobre o desempenho da loja</p>
          </div>
          <button
            onClick={imprimirRelatorio}
            className="flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-petroleo-900 to-petroleo-700 text-white rounded-lg font-semibold hover:shadow-lg transition"
          >
            <Printer size={20} />
            Imprimir Relatório
          </button>
        </div>

        {/* Cards de Resumo */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-white p-6 rounded-xl shadow-md">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Faturamento Total</p>
                <p className="text-2xl font-bold text-petroleo-700 mt-2">
                  R$ {faturamentoTotal.toFixed(2)}
                </p>
              </div>
              <div className="w-12 h-12 bg-petroleo-100 rounded-full flex items-center justify-center">
                <DollarSign className="text-petroleo-700" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-xl shadow-md">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Vendas Realizadas</p>
                <p className="text-2xl font-bold text-green-600 mt-2">
                  {pedidosPagos.length}
                </p>
              </div>
              <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
                <TrendingUp className="text-green-600" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-xl shadow-md">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Produtos Vendidos</p>
                <p className="text-2xl font-bold text-petroleo-700 mt-2">
                  {pedidosPagos.reduce((sum, p) =>
                    sum + p.itens.reduce((s, i) => s + i.quantidade, 0), 0
                  )}
                </p>
              </div>
              <div className="w-12 h-12 bg-petroleo-100 rounded-full flex items-center justify-center">
                <Package className="text-petroleo-700" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-xl shadow-md">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Clientes Únicos</p>
                <p className="text-2xl font-bold text-orange-600 mt-2">
                  {comprasPorCliente.size}
                </p>
              </div>
              <div className="w-12 h-12 bg-orange-100 rounded-full flex items-center justify-center">
                <Users className="text-orange-600" size={24} />
              </div>
            </div>
          </div>
        </div>

        {/* Gráficos */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Produtos Mais Vendidos */}
          <div className="bg-white p-6 rounded-xl shadow-md">
            <h2 className="text-xl font-semibold text-gray-800 mb-6">
              Produtos Mais Vendidos
            </h2>
            {produtosMaisVendidos.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={produtosMaisVendidos}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="nome" angle={-45} textAnchor="end" height={100} />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="quantidade" fill="#005f73" name="Quantidade Vendida" />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <p className="text-center text-gray-600 py-12">Nenhuma venda ainda</p>
            )}
          </div>

          {/* Vendas por Categoria */}
          <div className="bg-white p-6 rounded-xl shadow-md">
            <h2 className="text-xl font-semibold text-gray-800 mb-6">
              Vendas por Categoria
            </h2>
            {dadosCategoria.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={dadosCategoria}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={(entry) => entry.categoria}
                    outerRadius={100}
                    fill="#8884d8"
                    dataKey="valor"
                  >
                    {dadosCategoria.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(value: number) => `R$ ${value.toFixed(2)}`} />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <p className="text-center text-gray-600 py-12">Nenhuma venda ainda</p>
            )}
          </div>
        </div>

        {/* Tabelas */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Top Produtos */}
          <div className="bg-white p-6 rounded-xl shadow-md">
            <h2 className="text-xl font-semibold text-gray-800 mb-4">
              Top 5 Produtos por Faturamento
            </h2>
            <div className="space-y-3">
              {produtosMaisVendidos.map((produto, index) => (
                <div key={index} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <div className="flex items-center gap-3">
                    <span className="w-8 h-8 bg-petroleo-900 text-white rounded-full flex items-center justify-center font-bold text-sm">
                      {index + 1}
                    </span>
                    <div>
                      <p className="font-medium text-gray-800">{produto.nome}</p>
                      <p className="text-sm text-gray-600">{produto.quantidade} unidades</p>
                    </div>
                  </div>
                  <p className="font-bold text-petroleo-700">R$ {produto.valor.toFixed(2)}</p>
                </div>
              ))}
              {produtosMaisVendidos.length === 0 && (
                <p className="text-center text-gray-600 py-4">Nenhuma venda ainda</p>
              )}
            </div>
          </div>

          {/* Top Clientes */}
          <div className="bg-white p-6 rounded-xl shadow-md">
            <h2 className="text-xl font-semibold text-gray-800 mb-4">
              Top 5 Clientes
            </h2>
            <div className="space-y-3">
              {topClientes.map((cliente, index) => (
                <div key={index} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <div className="flex items-center gap-3">
                    <span className="w-8 h-8 bg-petroleo-800 text-white rounded-full flex items-center justify-center font-bold text-sm">
                      {index + 1}
                    </span>
                    <div>
                      <p className="font-medium text-gray-800">Cliente #{cliente.id}</p>
                      <p className="text-sm text-gray-600">{cliente.compras} compras</p>
                    </div>
                  </div>
                  <p className="font-bold text-petroleo-700">R$ {cliente.total.toFixed(2)}</p>
                </div>
              ))}
              {topClientes.length === 0 && (
                <p className="text-center text-gray-600 py-4">Nenhum cliente ainda</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};
