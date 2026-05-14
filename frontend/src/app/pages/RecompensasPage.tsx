import React, { useState, useEffect } from 'react';
import { Layout } from '../components/Layout';
import { useRewards } from '../contexts/RewardsContext';
import { useAuth } from '../contexts/AuthContext';
import {
  NIVEIS,
  calcularProximoNivel,
  calcularProgressoNivel,
  NivelTier,
} from '../types/rewards';
import {
  Trophy,
  Coins,
  TrendingUp,
  Award,
  Star,
  HelpCircle,
} from 'lucide-react';

export const RecompensasPage: React.FC = () => {
  const { rewards } = useRewards();
  const { user } = useAuth();
  const [apiUser, setApiUser] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUserData = async () => {
      if (user?.id) {
        try {
          const response = await fetch(`http://localhost:8080/api/usuarios/${user.id}`);
          if (response.ok) {
            const data = await response.json();
            setApiUser(data);
          }
        } catch (error) {
          console.error('Erro ao buscar dados do usuário:', error);
        } finally {
          setLoading(false);
        }
      }
    };

    fetchUserData();
  }, [user?.id]);

  if (!rewards || loading) {
    return (
      <Layout>
        <div className="flex flex-col items-center justify-center py-20">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-petroleo-600 mb-4"></div>
          <p className="text-gray-600">Carregando suas conquistas...</p>
        </div>
      </Layout>
    );
  }

  // Usar dados da API se disponíveis, caso contrário usar do context/localStorage
  const nivelApi = apiUser?.nivel?.toLowerCase() as NivelTier || rewards.nivel;
  const moedasApi = apiUser?.pontuacao !== undefined ? apiUser.pontuacao : rewards.moedas;

  const nivelAtual = NIVEIS[nivelApi] || NIVEIS.bronze;
  const proximoNivel = calcularProximoNivel(nivelApi);
  const progresso = calcularProgressoNivel(rewards.totalTransacoes, nivelApi);

  const getNivelIcon = (nivel: string) => {
    switch (nivel.toLowerCase()) {
      case 'bronze':
        return <Award size={32} style={{ color: NIVEIS.bronze.cor }} />;
      case 'prata':
        return <Trophy size={32} style={{ color: NIVEIS.prata.cor }} />;
      case 'ouro':
        return <Star size={32} style={{ color: NIVEIS.ouro.cor }} />;
      case 'diamante':
        return <Trophy size={32} style={{ color: NIVEIS.diamante.cor }} />;
      default:
        return <Award size={32} />;
    }
  };

  return (
    <Layout>
      <div className="max-w-6xl mx-auto space-y-8 pb-12">
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
          <div>
            <h1 className="text-4xl font-extrabold text-gray-900 tracking-tight">MarketRewards</h1>
            <p className="text-lg text-gray-600 mt-2">
              Sua jornada de benefícios e conquistas no MarketPay.
            </p>
          </div>
          <div className="flex items-center gap-2 bg-yellow-50 px-4 py-2 rounded-full border border-yellow-200">
            <Coins className="text-yellow-600" size={20} />
            <span className="font-bold text-yellow-700">{moedasApi} MarketCoins</span>
          </div>
        </div>

        {/* Hero Card do Nível Atual */}
        <div
          className="relative overflow-hidden rounded-3xl p-8 md:p-12 text-white shadow-2xl transition-all hover:shadow-petroleo-200/50"
          style={{
            background: `linear-gradient(135deg, ${nivelAtual.cor} 0%, ${nivelAtual.cor}aa 100%)`,
          }}
        >
          {/* Decorative background elements */}
          <div className="absolute top-0 right-0 -mt-20 -mr-20 w-64 h-64 bg-white/10 rounded-full blur-3xl"></div>
          <div className="absolute bottom-0 left-0 -mb-20 -ml-20 w-64 h-64 bg-black/10 rounded-full blur-3xl"></div>

          <div className="relative z-10 flex flex-col md:flex-row items-center md:items-start justify-between gap-8">
            <div className="flex flex-col md:flex-row items-center gap-6">
              <div className="w-24 h-24 bg-white/20 rounded-2xl flex items-center justify-center backdrop-blur-md border border-white/30 shadow-inner transform -rotate-3 hover:rotate-0 transition-transform duration-300">
                {getNivelIcon(nivelApi)}
              </div>
              <div className="text-center md:text-left">
                <p className="text-sm font-medium uppercase tracking-widest opacity-80">Nível Atual</p>
                <h2 className="text-5xl font-black mt-1 drop-shadow-sm">{nivelAtual.nome}</h2>
              </div>
            </div>


          </div>

          <div className="relative z-10 mt-12 space-y-4">
            <div className="flex items-center justify-between text-sm font-bold uppercase tracking-wider">
              <span>Progresso para {proximoNivel ? NIVEIS[proximoNivel].nome : 'Nível Máximo'}</span>
              <span>{progresso.toFixed(0)}%</span>
            </div>
            <div className="w-full h-5 bg-black/20 rounded-full overflow-hidden backdrop-blur-sm border border-white/10 shadow-inner">
              <div
                className="h-full bg-white rounded-full shadow-[0_0_15px_rgba(255,255,255,0.5)] transition-all duration-1000 ease-out"
                style={{ width: `${progresso}%` }}
              />
            </div>
          </div>
        </div>

        {/* Métricas e Benefícios */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-1 space-y-6">
            <div className="bg-white p-8 rounded-3xl shadow-lg border border-gray-100 hover:border-yellow-200 transition-colors">
              <div className="flex items-center justify-between mb-4">
                <div className="w-14 h-14 bg-yellow-100 rounded-2xl flex items-center justify-center">
                  <Coins className="text-yellow-600" size={30} />
                </div>
                <div className="text-right">
                  <p className="text-sm text-gray-500 font-medium">Saldo Atual</p>
                  <p className="text-4xl font-black text-gray-900">{moedasApi}</p>
                </div>
              </div>
              <h3 className="text-lg font-bold text-gray-800">MarketCoins</h3>
              <p className="text-sm text-gray-600 mt-2 leading-relaxed">
                Suas moedas acumuladas que podem ser trocadas por benefícios exclusivos no futuro.
              </p>
            </div>

            <div className="bg-white p-8 rounded-3xl shadow-lg border border-gray-100 hover:border-petroleo-200 transition-colors">
              <div className="flex items-center justify-between mb-4">
                <div className="w-14 h-14 bg-petroleo-100 rounded-2xl flex items-center justify-center">
                  <TrendingUp className="text-petroleo-700" size={30} />
                </div>
                <div className="text-right">
                  <p className="text-sm text-gray-500 font-medium">Histórico</p>
                  <p className="text-4xl font-black text-gray-900">{rewards.totalTransacoes}</p>
                </div>
              </div>
              <h3 className="text-lg font-bold text-gray-800">Total de Transações</h3>
              <p className="text-sm text-gray-600 mt-2 leading-relaxed">
                Contagem total de compras e vendas realizadas com sua conta.
              </p>
            </div>
          </div>

          <div className="lg:col-span-2 bg-white p-8 md:p-10 rounded-3xl shadow-lg border border-gray-100">
            <div className="flex items-center gap-3 mb-8">
              <div className="p-2 bg-petroleo-900 rounded-lg text-white">
                <Star size={24} />
              </div>
              <h3 className="text-2xl font-bold text-gray-900">
                Seus Benefícios {nivelAtual.nome}
              </h3>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="group p-6 bg-gray-50 rounded-2xl border border-transparent hover:border-petroleo-200 hover:bg-white transition-all duration-300">
                <div className="w-12 h-12 bg-white rounded-xl shadow-sm flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                  <Coins className="text-petroleo-700" size={24} />
                </div>
                <p className="font-bold text-gray-900 text-lg">Cashback em Dobro</p>
                <p className="text-sm text-gray-600 mt-1 leading-relaxed">
                  Ganhe {nivelAtual.recompensaPorcentagem}% do valor de cada transação de volta em MarketCoins.
                </p>
              </div>

              <div className="group p-6 bg-gray-50 rounded-2xl border border-transparent hover:border-orange-200 hover:bg-white transition-all duration-300">
                <div className="w-12 h-12 bg-white rounded-xl shadow-sm flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                  <Award className="text-orange-600" size={24} />
                </div>
                <p className="font-bold text-gray-900 text-lg">Status Prioritário</p>
                <p className="text-sm text-gray-600 mt-1 leading-relaxed">
                  Sua conta ganha destaque visual e prioridade em atendimentos do marketplace.
                </p>
              </div>

              <div className="group p-6 bg-gray-50 rounded-2xl border border-transparent hover:border-blue-200 hover:bg-white transition-all duration-300">
                <div className="w-12 h-12 bg-white rounded-xl shadow-sm flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                  <TrendingUp className="text-blue-600" size={24} />
                </div>
                <p className="font-bold text-gray-900 text-lg">Evolução Acelerada</p>
                <p className="text-sm text-gray-600 mt-1 leading-relaxed">
                  Multiplicadores de pontos especiais durante eventos sazonais do MarketPay.
                </p>
              </div>

              <div className="group p-6 bg-gray-50 rounded-2xl border border-transparent hover:border-purple-200 hover:bg-white transition-all duration-300">
                <div className="w-12 h-12 bg-white rounded-xl shadow-sm flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                  <HelpCircle className="text-purple-600" size={24} />
                </div>
                <p className="font-bold text-gray-900 text-lg">Suporte VIP</p>
                <p className="text-sm text-gray-600 mt-1 leading-relaxed">
                  Acesso a um canal exclusivo de suporte para resolver qualquer questão rapidamente.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Linha do Tempo de Níveis */}
        <div className="bg-white p-8 md:p-10 rounded-3xl shadow-lg border border-gray-100">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-10">
            <h3 className="text-2xl font-bold text-gray-900">Jornada de Níveis</h3>
            <div className="text-sm text-gray-500 font-medium">
              Realize transações para desbloquear novos patamares
            </div>
          </div>

          <div className="space-y-4">
            {Object.entries(NIVEIS).map(([key, nivel]) => {
              const isAtual = nivelApi === key;
              const isDesbloqueado = rewards.totalTransacoes >= nivel.transacoesNecessarias;

              return (
                <div
                  key={key}
                  className={`relative p-6 rounded-2xl border-2 transition-all duration-300 ${isAtual
                    ? 'border-petroleo-500 bg-petroleo-50/50 shadow-md transform scale-[1.01]'
                    : isDesbloqueado
                      ? 'border-green-100 bg-green-50/30'
                      : 'border-gray-100 bg-gray-50 opacity-70'
                    }`}
                >
                  <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                    <div className="flex items-center gap-5">
                      <div
                        className={`w-14 h-14 rounded-xl flex items-center justify-center shadow-sm ${isAtual ? 'bg-white' : 'bg-white'
                          }`}
                        style={{ backgroundColor: isAtual ? '#fff' : nivel.corBg }}
                      >
                        {getNivelIcon(key)}
                      </div>
                      <div>
                        <div className="flex items-center gap-3">
                          <h4 className="text-xl font-bold text-gray-900">{nivel.nome}</h4>
                          {isAtual && (
                            <span className="px-3 py-1 bg-petroleo-900 text-white text-[10px] font-black uppercase tracking-tighter rounded-full">
                              Seu Nível
                            </span>
                          )}
                          {isDesbloqueado && !isAtual && (
                            <div className="w-6 h-6 bg-green-500 rounded-full flex items-center justify-center">
                              <Award size={14} className="text-white" />
                            </div>
                          )}
                        </div>
                        <p className="text-sm text-gray-500 font-medium mt-1">
                          Requer {nivel.transacoesNecessarias} transações
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-8 px-4 py-2 bg-white/50 rounded-xl">
                      <div className="text-center">
                        <p className="text-[10px] uppercase font-bold text-gray-400">Cashback</p>
                        <p className="font-bold text-petroleo-700">{nivel.recompensaPorcentagem}%</p>
                      </div>
                      <div className="w-px h-8 bg-gray-200"></div>
                      <div className="text-center">
                        <p className="text-[10px] uppercase font-bold text-gray-400">Vantagem</p>
                        <p className="font-bold text-gray-800">Premium</p>
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Guia Rápido */}
        <div className="bg-gradient-to-br from-petroleo-950 via-petroleo-900 to-petroleo-800 p-8 md:p-12 rounded-[2rem] shadow-2xl text-white relative overflow-hidden">
          <div className="absolute top-0 right-0 w-64 h-64 bg-white/5 rounded-full -mt-20 -mr-20 blur-2xl"></div>

          <div className="relative z-10">
            <h3 className="text-3xl font-black mb-8">Como acelerar seus ganhos?</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
              <div className="space-y-3">
                <div className="w-10 h-10 bg-white/10 rounded-lg flex items-center justify-center font-black text-xl border border-white/20">
                  1
                </div>
                <h4 className="font-bold text-lg">Compre e Venda</h4>
                <p className="text-sm text-white/70 leading-relaxed">
                  Cada transação concluída conta para sua subida de nível.
                </p>
              </div>
              <div className="space-y-3">
                <div className="w-10 h-10 bg-white/10 rounded-lg flex items-center justify-center font-black text-xl border border-white/20">
                  2
                </div>
                <h4 className="font-bold text-lg">Acumule Moedas</h4>
                <p className="text-sm text-white/70 leading-relaxed">
                  MarketCoins são geradas automaticamente em cada operação.
                </p>
              </div>
              <div className="space-y-3">
                <div className="w-10 h-10 bg-white/10 rounded-lg flex items-center justify-center font-black text-xl border border-white/20">
                  3
                </div>
                <h4 className="font-bold text-lg">Suba de Rank</h4>
                <p className="text-sm text-white/70 leading-relaxed">
                  Novos níveis desbloqueiam porcentagens maiores de ganhos.
                </p>
              </div>
              <div className="space-y-3">
                <div className="w-10 h-10 bg-white/10 rounded-lg flex items-center justify-center font-black text-xl border border-white/20">
                  4
                </div>
                <h4 className="font-bold text-lg">Use os Privilégios</h4>
                <p className="text-sm text-white/70 leading-relaxed">
                  Aproveite o suporte VIP e o status exclusivo no marketplace.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};
