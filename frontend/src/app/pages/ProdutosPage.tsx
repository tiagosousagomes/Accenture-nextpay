import React, { useState } from 'react';
import { Layout } from '../components/Layout';
import { useMarketplace } from '../contexts/MarketplaceContext';
import { Package, Plus, Edit, Trash2, Store } from 'lucide-react';
import { toast } from 'sonner';
import { Produto } from '../types';

import { useAuth } from '../contexts/AuthContext';

export const ProdutosPage: React.FC = () => {
  const { user } = useAuth();
  const {
    getProdutosByVendedor,
    adicionarProduto,
    atualizarProduto,
    removerProduto,
    lojaAberta,
    toggleLoja,
  } = useMarketplace();

  const produtosDaLoja = getProdutosByVendedor(user?.id || '');

  const [modalAberto, setModalAberto] = useState(false);
  const [produtoEditando, setProdutoEditando] = useState<Produto | null>(null);
  const [formData, setFormData] = useState({
    sku: '',
    nome: '',
    categoria: '',
    preco: '',
    estoque: '',
    imagem: '',
  });
  const [isLoading, setIsLoading] = useState(false);
  const [produtoParaRemover, setProdutoParaRemover] = useState<any>(null);

  const handleAbrirModal = (produto?: Produto) => {
    if (produto) {
      setProdutoEditando(produto);
      setFormData({
        sku: produto.id, // No backend, SKU é o ID
        nome: produto.nome,
        categoria: produto.descricao || '', // No backend, Categoria é Descrição
        preco: produto.preco.toString(),
        estoque: produto.estoque.toString(),
        imagem: produto.imagem || '',
      });
    } else {
      setProdutoEditando(null);
      setFormData({ sku: '', nome: '', categoria: '', preco: '', estoque: '', imagem: '' });
    }
    setModalAberto(true);
  };

  const handleFecharModal = () => {
    setModalAberto(false);
    setProdutoEditando(null);
    setFormData({ sku: '', nome: '', categoria: '', preco: '', estoque: '', imagem: '' });
  };

  const handleFotoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setFormData(prev => ({ ...prev, imagem: reader.result as string }));
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSalvar = async () => {
    if (!formData.nome || !formData.preco || !formData.estoque) {
      toast.error('Preencha os campos obrigatórios (Nome, Preço e Estoque)');
      return;
    }

    setIsLoading(true);
    try {
      const precoNum = parseFloat(formData.preco);
      const estoqueNum = parseInt(formData.estoque);

      if (isNaN(precoNum) || isNaN(estoqueNum)) {
        toast.error('Preço e estoque devem ser números válidos');
        return;
      }

      const dadosProduto = {
        id: produtoEditando ? parseInt(produtoEditando.id) : (formData.sku ? parseInt(formData.sku) : undefined),
        nome: formData.nome,
        descricao: formData.categoria || '',
        preco: precoNum,
        estoque: estoqueNum,
        imagem: formData.imagem,
      };

      let sucesso = false;
      if (produtoEditando) {
        sucesso = await atualizarProduto(produtoEditando.id, dadosProduto);
        if (sucesso) toast.success('Produto atualizado com sucesso!');
      } else {
        sucesso = await adicionarProduto(dadosProduto);
        if (sucesso) toast.success('Produto adicionado com sucesso!');
      }

      if (sucesso) {
        handleFecharModal();
      } else {
        toast.error('Erro ao salvar produto no servidor');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleRemover = (produto: any) => {
    setProdutoParaRemover(produto);
  };

  return (
    <Layout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-800">Gestão de Produtos</h1>
            <p className="text-gray-600 mt-1">Gerencie o catálogo da sua loja</p>
          </div>
          <div className="flex gap-3">
            <button
              onClick={toggleLoja}
              className={`flex items-center gap-2 px-6 py-3 rounded-lg font-semibold transition ${
                lojaAberta
                  ? 'bg-green-600 text-white hover:bg-green-700'
                  : 'bg-red-600 text-white hover:bg-red-700'
              }`}
            >
              <Store size={20} />
              {lojaAberta ? 'Loja Aberta' : 'Loja Fechada'}
            </button>
            <button
              onClick={() => handleAbrirModal()}
              className="flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-cyan-700 to-teal-600 text-white rounded-lg font-semibold hover:shadow-lg transition"
            >
              <Plus size={20} />
              Novo Produto
            </button>
          </div>
        </div>

        {/* Estatísticas */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white p-6 rounded-xl shadow-md">
            <p className="text-sm text-gray-600">Total de Produtos</p>
            <p className="text-3xl font-bold text-gray-800 mt-2">{produtosDaLoja.length}</p>
          </div>
          <div className="bg-white p-6 rounded-xl shadow-md">
            <p className="text-sm text-gray-600">Produtos em Estoque</p>
            <p className="text-3xl font-bold text-green-600 mt-2">
              {produtosDaLoja.filter(p => p.estoque > 0).length}
            </p>
          </div>
          <div className="bg-white p-6 rounded-xl shadow-md">
            <p className="text-sm text-gray-600">Produtos Sem Estoque</p>
            <p className="text-3xl font-bold text-red-600 mt-2">
              {produtosDaLoja.filter(p => p.estoque === 0).length}
            </p>
          </div>
        </div>

        {/* Tabela de Produtos */}
        <div className="bg-white rounded-xl shadow-md overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b">
                <tr>
                  <th className="text-left p-4 font-semibold text-gray-700 w-16">Foto</th>
                  <th className="text-left p-4 font-semibold text-gray-700">Código (ID)</th>
                  <th className="text-left p-4 font-semibold text-gray-700">Nome</th>
                  <th className="text-left p-4 font-semibold text-gray-700">Descrição</th>
                  <th className="text-right p-4 font-semibold text-gray-700">Preço</th>
                  <th className="text-right p-4 font-semibold text-gray-700">Estoque</th>
                  <th className="text-center p-4 font-semibold text-gray-700">Ações</th>
                </tr>
              </thead>
              <tbody>
                {produtosDaLoja.map((produto) => (
                  <tr key={produto.id} className="border-b hover:bg-gray-50 transition">
                    <td className="p-4">
                      <div className="w-10 h-10 rounded overflow-hidden bg-gray-100 flex items-center justify-center">
                        {produto.imagem ? (
                          <img src={produto.imagem} alt={produto.nome} className="w-full h-full object-cover" />
                        ) : (
                          <Package size={20} className="text-gray-400" />
                        )}
                      </div>
                    </td>
                    <td className="p-4 text-sm text-gray-700">{produto.id}</td>
                    <td className="p-4 font-medium text-gray-800">{produto.nome}</td>
                    <td className="p-4">
                      <span className="inline-block px-2 py-1 bg-cyan-100 text-cyan-700 text-xs rounded max-w-[200px] truncate">
                        {produto.descricao || 'Sem descrição'}
                      </span>
                    </td>
                    <td className="p-4 text-right font-semibold text-gray-800">
                      R$ {produto.preco.toFixed(2)}
                    </td>
                    <td className="p-4 text-right">
                      <span className={`font-semibold ${produto.estoque > 0 ? 'text-green-600' : 'text-red-600'}`}>
                        {produto.estoque}
                      </span>
                    </td>
                    <td className="p-4">
                      <div className="flex items-center justify-center gap-2">
                        <button
                          onClick={() => handleAbrirModal(produto)}
                          className="p-2 text-cyan-600 hover:bg-cyan-50 rounded-lg transition"
                        >
                          <Edit size={18} />
                        </button>
                        <button
                          onClick={() => handleRemover(produto)}
                          className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition"
                        >
                          <Trash2 size={18} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            {produtosDaLoja.length === 0 && (
              <div className="text-center py-12">
                <Package className="mx-auto text-gray-400 mb-4" size={48} />
                <p className="text-gray-600">Nenhum produto cadastrado</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Modal de Produto */}
      {modalAberto && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl p-6 w-full max-w-md max-h-[90vh] overflow-y-auto">
            <h2 className="text-2xl font-bold text-gray-800 mb-6">
              {produtoEditando ? 'Editar Produto' : 'Novo Produto'}
            </h2>

            <div className="space-y-4">
              {/* Foto do Produto */}
              <div className="flex flex-col items-center justify-center mb-2">
                <div className="w-24 h-24 bg-gray-100 flex items-center justify-center border-2 border-dashed border-gray-300 overflow-hidden mb-2 rounded-lg">
                  {formData.imagem ? (
                    <img src={formData.imagem} alt="Preview" className="w-full h-full object-cover" />
                  ) : (
                    <span className="text-xs text-gray-400 text-center px-2">Foto do Produto</span>
                  )}
                </div>
                <label className="cursor-pointer text-sm font-medium text-cyan-600 hover:text-cyan-700">
                  Escolher imagem
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleFotoChange}
                    className="hidden"
                  />
                </label>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Código (ID)</label>
                <input
                  type="text"
                  value={formData.sku}
                  onChange={(e) => setFormData({ ...formData, sku: e.target.value })}
                  disabled={!!produtoEditando}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-cyan-500 focus:border-transparent outline-none disabled:bg-gray-50 disabled:text-gray-500"
                  placeholder="Gerado automaticamente"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Nome *</label>
                <input
                  type="text"
                  value={formData.nome}
                  onChange={(e) => setFormData({ ...formData, nome: e.target.value })}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-cyan-500 focus:border-transparent outline-none"
                  placeholder="Nome do produto"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Descrição *</label>
                <input
                  type="text"
                  value={formData.categoria}
                  onChange={(e) => setFormData({ ...formData, categoria: e.target.value })}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-cyan-500 focus:border-transparent outline-none"
                  placeholder="Breve descrição do produto"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Preço *</label>
                  <input
                    type="number"
                    value={formData.preco}
                    onChange={(e) => setFormData({ ...formData, preco: e.target.value })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-cyan-500 focus:border-transparent outline-none"
                    placeholder="0.00"
                    step="0.01"
                    min="0"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Estoque *</label>
                  <input
                    type="number"
                    value={formData.estoque}
                    onChange={(e) => setFormData({ ...formData, estoque: e.target.value })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-cyan-500 focus:border-transparent outline-none"
                    placeholder="0"
                    min="0"
                  />
                </div>
              </div>
            </div>

            <div className="flex gap-3 mt-6">
              <button
                onClick={handleFecharModal}
                disabled={isLoading}
                className="flex-1 px-4 py-3 bg-gray-200 text-gray-700 rounded-lg font-semibold hover:bg-gray-300 transition disabled:opacity-50"
              >
                Cancelar
              </button>
              <button
                onClick={handleSalvar}
                disabled={isLoading}
                className="flex-1 px-4 py-3 bg-gradient-to-r from-cyan-700 to-teal-600 text-white rounded-lg font-semibold hover:shadow-lg transition flex items-center justify-center disabled:opacity-70 disabled:cursor-not-allowed"
              >
                {isLoading ? (
                  <svg className="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                ) : 'Salvar'}
              </button>
            </div>
          </div>
        </div>
      )}

      {produtoParaRemover && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl shadow-2xl p-6 w-full max-w-md">
            <h2 className="text-xl font-bold text-gray-900 mb-2">
              Remover produto
            </h2>

            <p className="text-gray-600 mb-6">
              Deseja realmente remover o produto "{produtoParaRemover.nome}"?
            </p>

            <div className="flex justify-end gap-3">
              <button
                onClick={() => setProdutoParaRemover(null)}
                className="px-5 py-2 rounded-lg bg-gray-100 text-gray-700 font-semibold hover:bg-gray-200 transition"
              >
                Cancelar
              </button>

              <button
                onClick={async () => {
                  setIsLoading(true);

                  try {
                    const sucesso = await removerProduto(produtoParaRemover.id);

                    if (sucesso) {
                      toast.success('Produto removido com sucesso!');
                    } else {
                      toast.error('Erro ao remover produto');
                    }
                  } catch {
                    toast.error('Erro ao remover produto');
                  } finally {
                    setIsLoading(false);
                    setProdutoParaRemover(null);
                  }
                }}
                className="px-5 py-2 rounded-lg bg-red-600 text-white font-semibold hover:bg-red-700 transition"
              >
                Remover
              </button>
            </div>
          </div>
        </div>
      )}
      
    </Layout>
  );
};
