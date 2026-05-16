import React, { useState } from 'react';
import { Layout } from '../components/Layout';
import { useBank } from '../contexts/BankContext';
import { useAuth } from '../contexts/AuthContext';
import {
  ArrowDownCircle,
  ArrowUpCircle,
  FileText,
  Download,
  Search,
  Calendar,
} from 'lucide-react';
import { jsPDF } from 'jspdf';
import { toast } from 'sonner';
import { Transacao } from '../types';

export const ExtratoPage: React.FC = () => {
  const { conta, transacoes } = useBank();
  const { user } = useAuth();
  const [filtro, setFiltro] = useState('');
  const [tipoFiltro, setTipoFiltro] = useState('todos');

  const isEntradaTransacao = (tipo: string, descricao?: string) => {
    if (
      tipo === 'deposito' ||
      tipo === 'transferencia_recebida' ||
      tipo === 'pix_recebido'
    ) {
      return true;
    }
    if (
      descricao?.toLowerCase().includes('recebido')
    ) {
      return true;
    }
    return false;
  };

  const isSaidaTransacao = (tipo: string, descricao?: string) => {
    return (
      tipo === 'saque' ||
      tipo === 'compra' ||
      tipo === 'transferencia_enviada' ||
      tipo === 'pix_enviado' ||
      tipo === 'pagamento_pedido' ||
      tipo === 'transferencia' ||
      descricao?.toLowerCase().includes('enviado') ||
      descricao?.toLowerCase().includes('compra')
    );
  };

  const transacoesFiltradas = transacoes.filter((t) => {
    const matchBusca = t.descricao
      .toLowerCase()
      .includes(filtro.toLowerCase());

    const matchTipo =
      tipoFiltro === 'todos' ||
      (tipoFiltro === 'entradas' && isEntradaTransacao(t.tipo, t.descricao)) ||
      (tipoFiltro === 'saidas' && isSaidaTransacao(t.tipo, t.descricao));

    return matchBusca && matchTipo;
  });

  const gerarPDF = () => {
    const doc = new jsPDF();

    // Cabeçalho
    doc.setFontSize(20);
    doc.text('NextPay - Extrato Bancário', 20, 20);

    doc.setFontSize(12);
    doc.text(`Cliente: ${user?.nome} ${user?.sobrenome}`, 20, 35);
    doc.text(`CPF: ${user?.cpf}`, 20, 42);
    doc.text(`Conta: ${conta?.numero}`, 20, 49);
    doc.text(`Saldo Atual: R$ ${conta?.saldo.toFixed(2)}`, 20, 56);
    doc.text(`Data: ${new Date().toLocaleDateString('pt-BR')}`, 20, 63);

    // Linha divisória
    doc.line(20, 68, 190, 68);

    // Transações
    doc.setFontSize(14);
    doc.text('Histórico de Transações', 20, 78);

    doc.setFontSize(10);
    let y = 88;

    transacoesFiltradas.forEach((t, index) => {
      if (y > 270) {
        doc.addPage();
        y = 20;
      }

      const isEntrada = isEntradaTransacao(t.tipo, t.descricao);
      const sinal = isEntrada ? '+' : '-';

      doc.text(`${new Date(t.data).toLocaleString('pt-BR')}`, 20, y);
      doc.text(t.descricao, 20, y + 5);
      doc.text(`${sinal} R$ ${t.valor.toFixed(2)}`, 160, y);

      y += 15;

      if (index < transacoesFiltradas.length - 1) {
        doc.line(20, y - 5, 190, y - 5);
      }
    });

    doc.save(`extrato-${conta?.numero}-${new Date().getTime()}.pdf`);
    toast.success('Extrato baixado com sucesso!');
  };

  const getTipoIcon = (tipo: Transacao['tipo'], descricao?: string) => {
    const isEntrada = isEntradaTransacao(tipo, descricao);
    if (isEntrada) {
      return <ArrowDownCircle className="text-green-600" size={24} />;
    }
    return <ArrowUpCircle className="text-red-600" size={24} />;
  };

  const getTipoColor = (tipo: Transacao['tipo'], descricao?: string) => {
    const isEntrada = isEntradaTransacao(tipo, descricao);
    if (isEntrada) {
      return 'bg-green-100';
    }
    return 'bg-red-100';
  };

  const getValorColor = (tipo: Transacao['tipo'], descricao?: string) => {
    const isEntrada = isEntradaTransacao(tipo, descricao);
    if (isEntrada) {
      return 'text-green-600';
    }
    return 'text-red-600';
  };

  const getSinal = (tipo: Transacao['tipo'], descricao?: string) => {
    const isEntrada = isEntradaTransacao(tipo, descricao);
    if (isEntrada) {
      return '+';
    }
    return '-';
  };

  return (
    <Layout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-800">Extrato Bancário</h1>
            <p className="text-gray-600 mt-1">Histórico completo de transações</p>
          </div>
          <button
            onClick={gerarPDF}
            className="flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-petroleo-900 to-petroleo-700 text-white rounded-lg font-semibold hover:shadow-lg transition"
          >
            <Download size={20} />
            Baixar PDF
          </button>
        </div>

        {/* Card de Informações */}
        <div className="bg-white p-6 rounded-xl shadow-md">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div>
              <p className="text-sm text-gray-600">Conta Corrente</p>
              <p className="text-xl font-semibold text-gray-800">{conta?.numero}</p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Saldo Atual</p>
              <p className="text-xl font-semibold text-petroleo-700">R$ {conta?.saldo.toFixed(2)}</p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Total de Transações</p>
              <p className="text-xl font-semibold text-gray-800">{transacoes.length}</p>
            </div>
          </div>
        </div>

        {/* Filtros */}
        <div className="bg-white p-6 rounded-xl shadow-md">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
              <input
                type="text"
                value={filtro}
                onChange={(e) => setFiltro(e.target.value)}
                className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none"
                placeholder="Buscar transação..."
              />
            </div>
            <select
              value={tipoFiltro}
              onChange={(e) => setTipoFiltro(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none"
            >
              <option value="todos">Todas as Transações</option>
              <option value="entradas">Apenas Entradas</option>
              <option value="saidas">Apenas Saídas</option>
            </select>
          </div>
        </div>

        {/* Timeline de Transações */}
        <div className="bg-white p-6 rounded-xl shadow-md">
          <div className="flex items-center gap-2 mb-6">
            <FileText className="text-petroleo-700" size={24} />
            <h2 className="text-xl font-semibold text-gray-800">Histórico de Transações</h2>
          </div>

          <div className="space-y-0">
            {transacoesFiltradas.map((transacao, index) => (
              <div key={transacao.id} className="relative">
                {/* Linha vertical da timeline */}
                {index < transacoesFiltradas.length - 1 && (
                  <div className="absolute left-6 top-16 bottom-0 w-0.5 bg-gray-200" />
                )}

                <div className="flex gap-4 pb-8">
                  {/* Ícone */}
                  <div className={`w-12 h-12 rounded-full flex items-center justify-center flex-shrink-0 ${getTipoColor(transacao.tipo, transacao.descricao)} relative z-10`}>
                    {getTipoIcon(transacao.tipo, transacao.descricao)}
                  </div>

                  {/* Conteúdo */}
                  <div className="flex-1 bg-gray-50 rounded-lg p-4">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <h3 className="font-semibold text-gray-800">{transacao.descricao}</h3>
                        <div className="flex items-center gap-2 mt-1 text-sm text-gray-600">
                          <Calendar size={14} />
                          <span>{new Date(transacao.data).toLocaleString('pt-BR')}</span>
                        </div>
                        <div className="mt-2">
                          <span className={`inline-block px-3 py-1 rounded-full text-xs font-semibold ${transacao.status === 'sucesso'
                            ? 'bg-green-100 text-green-700'
                            : 'bg-red-100 text-red-700'
                            }`}>
                            {transacao.status === 'sucesso' ? 'Concluída' : 'Falhou'}
                          </span>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className={`text-2xl font-bold ${getValorColor(transacao.tipo, transacao.descricao)}`}>
                          {getSinal(transacao.tipo)} R$ {transacao.valor.toFixed(2)}
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            ))}

            {transacoesFiltradas.length === 0 && (
              <div className="text-center py-12">
                <FileText className="mx-auto text-gray-400 mb-4" size={48} />
                <p className="text-gray-600">Nenhuma transação encontrada</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </Layout>
  );
};
