import React, { useState, useEffect } from 'react';
import { Layout } from '../components/Layout';
import { useAuth } from '../contexts/AuthContext';
import {
  User,
  Mail,
  MapPin,
  CreditCard,
  Store,
  Calendar,
  Award,
  Coins,
  ChevronRight,
  ShieldCheck
} from 'lucide-react';
import { toast } from 'sonner';

export const PerfilPage: React.FC = () => {
  const { user, updateProfile } = useAuth();
  const [apiUser, setApiUser] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [editando, setEditando] = useState(false);
  const [fotoPreview, setFotoPreview] = useState('');
  const [formData, setFormData] = useState({
    nome: '',
    sobrenome: '',
    endereco: '',
    nomeLoja: '',
    descricaoLoja: '',
  });

  useEffect(() => {
    const fetchUserData = async () => {
      if (user?.id) {
        try {
          const response = await fetch(`http://localhost:8080/api/usuarios/${user.id}`);
          if (response.ok) {
            const data = await response.json();
            setApiUser(data);

            // Formatar endereço se for um objeto
            let enderecoStr = '';
            if (data.endereco && typeof data.endereco === 'object') {
              const { logradouro, numero, bairro, localidade, uf } = data.endereco;
              enderecoStr = `${logradouro}, ${numero} - ${bairro}, ${localidade}/${uf}`;
            } else {
              enderecoStr = data.endereco || user?.endereco || '';
            }

            setFormData({
              nome: data.nome || user?.nome || '',
              sobrenome: user?.sobrenome || '', // API não parece retornar sobrenome separado
              endereco: enderecoStr,
              nomeLoja: user?.nomeLoja || '',
              descricaoLoja: user?.descricaoLoja || '',
            });
          }
        } catch (error) {
          console.error('Erro ao buscar dados do perfil:', error);
        } finally {
          setLoading(false);
        }
      }
    };

    fetchUserData();
  }, [user]);

  const handleSalvar = () => {
    updateProfile(formData);
    toast.success('Perfil atualizado com sucesso!');
    setEditando(false);
  };

  const handleCancelar = () => {
    if (apiUser) {
      let enderecoStr = '';
      if (apiUser.endereco && typeof apiUser.endereco === 'object') {
        const { logradouro, numero, bairro, localidade, uf } = apiUser.endereco;
        enderecoStr = `${logradouro}, ${numero} - ${bairro}, ${localidade}/${uf}`;
      }
      setFormData({
        nome: apiUser.nome || user?.nome || '',
        sobrenome: user?.sobrenome || '',
        endereco: enderecoStr,
        nomeLoja: user?.nomeLoja || '',
        descricaoLoja: user?.descricaoLoja || '',
      });
    }
    setEditando(false);
  };

  if (loading) {
    return (
      <Layout>
        <div className="flex flex-col items-center justify-center py-20">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-petroleo-600 mb-4"></div>
          <p className="text-gray-600">Carregando seu perfil...</p>
        </div>
      </Layout>
    );
  }

  const nivelColor = apiUser?.nivel === 'OURO' ? 'text-yellow-600 bg-yellow-50' :
    apiUser?.nivel === 'DIAMANTE' ? 'text-blue-600 bg-blue-50' :
      apiUser?.nivel === 'PRATA' ? 'text-gray-600 bg-gray-50' : 'text-orange-600 bg-orange-50';

  return (
    <Layout>
      <div className="max-w-5xl mx-auto space-y-8 pb-12">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <h1 className="text-4xl font-extrabold text-gray-900 tracking-tight">Meu Perfil</h1>
            <p className="text-lg text-gray-600 mt-1">Gerencie sua identidade no ecossistema MarketPay.</p>
          </div>
          <div className="flex items-center gap-3">
            <div className={`flex items-center gap-2 px-4 py-2 rounded-full border border-current font-bold text-sm ${nivelColor}`}>
              <Award size={18} />
              <span>Nível {apiUser?.nivel || 'BRONZE'}</span>
            </div>
            <div className="flex items-center gap-2 bg-yellow-50 px-4 py-2 rounded-full border border-yellow-200 text-yellow-700 font-bold text-sm">
              <Coins size={18} />
              <span>{apiUser?.pontuacao || 0} MarketCoins</span>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Card Esquerdo: Visão Geral */}
          <div className="lg:col-span-1 space-y-6">
            <div className="bg-white rounded-3xl shadow-xl overflow-hidden border border-gray-100">
              <div className="h-32 bg-gradient-to-br from-petroleo-900 via-petroleo-800 to-petroleo-700 relative">
                <div className="absolute -bottom-12 left-1/2 -translate-x-1/2">
                  <div className="w-24 h-24 bg-white rounded-2xl p-1 shadow-2xl">
                    <div className="w-full h-full bg-gray-50 rounded-xl flex items-center justify-center border border-gray-100">
                      {(fotoPreview || apiUser?.fotoPerfilUrl) ? (
                          <img
                            src={fotoPreview || apiUser?.fotoPerfilUrl}
                            alt="Perfil"
                            className="w-full h-full object-cover rounded-xl"
                          />
                        ) : (
                          <User className="text-petroleo-700" size={48} />
                        )}
                    </div>
                  </div>
                </div>
              </div>

              <div className="pt-16 pb-8 px-6 text-center">
                 {editando && (
                    <div className="mb-4 flex justify-center">
                      <label className="cursor-pointer bg-white rounded-lg px-4 py-2 shadow-md text-sm font-bold text-petroleo-700 hover:bg-gray-100 transition border border-gray-200">
                        Alterar Foto

                        <input
                          type="file"
                          accept="image/*"
                          className="hidden"
                          onChange={(e) => {
                            const file = e.target.files?.[0];

                            if (file) {
                              const reader = new FileReader();

                              reader.onloadend = () => {
                                setFotoPreview(reader.result as string);
                              };

                              reader.readAsDataURL(file);
                            }
                          }}
                        />
                      </label>
                    </div>
                  )}
                <h2 className="text-2xl font-bold text-gray-900">{apiUser?.nome || user?.nome}</h2>
                <p className="text-gray-500 font-medium text-sm mt-1">{apiUser?.email}</p>

                <div className="mt-6 pt-6 border-t border-gray-50 grid grid-cols-2 gap-4">
                  <div className="text-center">
                    <p className="text-[10px] uppercase font-bold text-gray-400 tracking-wider">Conta</p>
                    <p className="font-bold text-gray-800 mt-1">Ativa</p>
                  </div>
                  <div className="text-center">
                    <p className="text-[10px] uppercase font-bold text-gray-400 tracking-wider">Verificado</p>
                    <div className="flex justify-center mt-1">
                      <ShieldCheck className="text-green-500" size={18} />
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div className="bg-white p-6 rounded-3xl shadow-lg border border-gray-100">
              <h3 className="font-bold text-gray-900 mb-4 flex items-center gap-2">
                <Calendar size={18} className="text-petroleo-700" />
                Atividade
              </h3>
              <div className="space-y-4">
                <div className="flex items-center justify-between text-sm">
                  <span className="text-gray-500">Membro desde</span>
                  <span className="font-semibold text-gray-800">
                    {user ? new Date(user.createdAt).toLocaleDateString('pt-BR') : 'Maio 2024'}
                  </span>
                </div>
                <div className="flex items-center justify-between text-sm">
                  <span className="text-gray-500">Último Acesso</span>
                  <span className="font-semibold text-gray-800">Hoje</span>
                </div>
              </div>
            </div>
          </div>

          {/* Coluna Direita: Formulário */}
          <div className="lg:col-span-2 space-y-6">
            <div className="bg-white rounded-3xl shadow-xl border border-gray-100 overflow-hidden">
              <div className="p-8 md:p-10">
                <div className="flex items-center justify-between mb-8">
                  <h3 className="text-2xl font-bold text-gray-900">Informações Pessoais</h3>
                  {!editando && (
                    <button
                      onClick={() => setEditando(true)}
                      className="text-petroleo-700 font-bold text-sm flex items-center gap-1 hover:underline"
                    >
                      Editar Informações
                      <ChevronRight size={16} />
                    </button>
                  )}
                </div>

                <div className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="space-y-2">
                      <label className="text-xs font-bold text-gray-400 uppercase tracking-wider ml-1">Nome Completo</label>
                      {editando ? (
                        <input
                          type="text"
                          value={formData.nome}
                          onChange={(e) => setFormData({ ...formData, nome: e.target.value })}
                          className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-petroleo-500 outline-none transition-all"
                        />
                      ) : (
                        <div className="flex items-center gap-3 px-4 py-3 bg-gray-50 rounded-xl border border-transparent">
                          <User size={18} className="text-gray-400" />
                          <span className="text-gray-800 font-medium">{apiUser?.nome || user?.nome}</span>
                        </div>
                      )}
                    </div>

                    <div className="space-y-2">
                      <label className="text-xs font-bold text-gray-400 uppercase tracking-wider ml-1">Documento (CPF/CNPJ)</label>
                      <div className="flex items-center gap-3 px-4 py-3 bg-gray-100/50 rounded-xl border border-transparent grayscale">
                        <CreditCard size={18} className="text-gray-400" />
                        <span className="text-gray-500 font-medium">{apiUser?.cpfCnpj || user?.cpf}</span>
                      </div>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <label className="text-xs font-bold text-gray-400 uppercase tracking-wider ml-1">Endereço de Cobrança</label>
                    {editando ? (
                      <div className="relative">
                        <MapPin className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                        <input
                          type="text"
                          value={formData.endereco}
                          onChange={(e) => setFormData({ ...formData, endereco: e.target.value })}
                          className="w-full pl-12 pr-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-petroleo-500 outline-none transition-all"
                        />
                      </div>
                    ) : (
                      <div className="flex items-start gap-3 px-4 py-3 bg-gray-50 rounded-xl border border-transparent">
                        <MapPin size={18} className="text-gray-400 mt-0.5" />
                        <span className="text-gray-800 font-medium">{formData.endereco || 'Endereço não cadastrado'}</span>
                      </div>
                    )}
                  </div>

                  <div className="pt-6 border-t border-gray-50 mt-8">
                    <h4 className="text-lg font-bold text-gray-900 mb-6 flex items-center gap-2">
                      <Store size={20} className="text-petroleo-700" />
                      Configurações de Loja
                    </h4>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="space-y-2">
                        <label className="text-xs font-bold text-gray-400 uppercase tracking-wider ml-1">Nome da Vitrine</label>
                        {editando ? (
                          <input
                            type="text"
                            value={formData.nomeLoja}
                            onChange={(e) => setFormData({ ...formData, nomeLoja: e.target.value })}
                            className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-petroleo-500 outline-none transition-all"
                            placeholder="Minha Loja Online"
                          />
                        ) : (
                          <div className="px-4 py-3 bg-gray-50 rounded-xl font-medium text-gray-800">
                            {user?.nomeLoja || 'Não configurado'}
                          </div>
                        )}
                      </div>

                      <div className="space-y-2">
                        <label className="text-xs font-bold text-gray-400 uppercase tracking-wider ml-1">Bio/Descrição</label>
                        {editando ? (
                          <input
                            type="text"
                            value={formData.descricaoLoja}
                            onChange={(e) => setFormData({ ...formData, descricaoLoja: e.target.value })}
                            className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-petroleo-500 outline-none transition-all"
                            placeholder="O que você oferece?"
                          />
                        ) : (
                          <div className="px-4 py-3 bg-gray-50 rounded-xl font-medium text-gray-800 truncate">
                            {user?.descricaoLoja || 'Sem descrição definida'}
                          </div>
                        )}
                      </div>
                    </div>
                  </div>

                  {editando && (
                    <div className="flex gap-4 pt-8">
                      <button
                        onClick={handleCancelar}
                        className="flex-1 px-6 py-4 bg-gray-100 text-gray-600 rounded-2xl font-bold hover:bg-gray-200 transition-all"
                      >
                        Descartar
                      </button>
                      <button
                        onClick={handleSalvar}
                        className="flex-1 px-6 py-4 bg-petroleo-900 text-white rounded-2xl font-bold hover:shadow-xl hover:shadow-petroleo-900/20 transition-all shadow-lg"
                      >
                        Salvar Alterações
                      </button>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};
