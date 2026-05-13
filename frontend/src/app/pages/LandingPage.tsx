import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  CreditCard,
  ShoppingBag,
  TrendingUp,
  Shield,
  Zap,
  Gift,
  Award,
  ArrowRight,
  CheckCircle,
  Sparkles,
} from 'lucide-react';

export const LandingPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-white">
      {/* Header/Nav */}
      <nav className="fixed top-0 w-full bg-white/80 backdrop-blur-lg border-b z-50">
        <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
          <h1 className="text-2xl font-bold bg-gradient-to-r from-petroleo-900 to-petroleo-700 bg-clip-text text-transparent">
            MarketPay
          </h1>
          <div className="flex gap-3">
            <button
              onClick={() => navigate('/login')}
              className="px-6 py-2 text-gray-700 hover:text-petroleo-700 transition"
            >
              Login
            </button>
            <button
              onClick={() => navigate('/cadastro')}
              className="px-6 py-2 bg-gradient-to-r from-petroleo-900 to-petroleo-700 text-white rounded-lg font-semibold hover:shadow-lg transition"
            >
              Criar Conta
            </button>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="pt-32 pb-20 px-6">
        <div className="max-w-7xl mx-auto">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
            <div>
              <div className="inline-block px-4 py-2 bg-petroleo-100 text-petroleo-800 rounded-full text-sm font-semibold mb-6">
                🚀 Banco Digital + Marketplace
              </div>
              <h1 className="text-6xl font-bold text-gray-900 mb-6">
                Seu dinheiro e
                <span className="bg-gradient-to-r from-petroleo-900 to-petroleo-700 bg-clip-text text-transparent">
                  {' '}compras{' '}
                </span>
                em um só lugar
              </h1>
              <p className="text-xl text-gray-600 mb-8">
                Banco digital completo integrado com marketplace. Compre, pague, ganhe recompensas e suba de nível!
              </p>
              <div className="flex gap-4">
                <button
                  onClick={() => navigate('/cadastro')}
                  className="px-8 py-4 bg-gradient-to-r from-petroleo-900 to-petroleo-700 text-white rounded-lg font-semibold hover:shadow-xl transition flex items-center gap-2"
                >
                  Começar Agora
                  <ArrowRight size={20} />
                </button>
                <button
                  onClick={() => document.getElementById('recursos')?.scrollIntoView({ behavior: 'smooth' })}
                  className="px-8 py-4 border-2 border-gray-300 text-gray-700 rounded-lg font-semibold hover:border-petroleo-700 hover:text-petroleo-700 transition"
                >
                  Saiba Mais
                </button>
              </div>
            </div>

            <div className="relative">
              <div className="bg-gradient-to-br from-petroleo-800 to-petroleo-600 rounded-3xl p-8 shadow-2xl">
                <div className="bg-white/10 backdrop-blur-lg rounded-2xl p-6 text-white mb-4">
                  <div className="flex items-center justify-between mb-4">
                    <span className="text-sm opacity-80">Saldo Disponível</span>
                    <CreditCard size={24} />
                  </div>
                  <p className="text-4xl font-bold">R$ 5.000,00</p>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="bg-white/10 backdrop-blur-lg rounded-xl p-4 text-white">
                    <ShoppingBag size={20} className="mb-2" />
                    <p className="text-2xl font-bold">127</p>
                    <p className="text-sm opacity-80">Compras</p>
                  </div>
                  <div className="bg-white/10 backdrop-blur-lg rounded-xl p-4 text-white">
                    <Gift size={20} className="mb-2" />
                    <p className="text-2xl font-bold">850</p>
                    <p className="text-sm opacity-80">MarketCoins</p>
                  </div>
                </div>
              </div>
              <div className="absolute -top-6 -right-6 w-32 h-32 bg-yellow-400 rounded-full flex items-center justify-center shadow-xl">
                <Award size={64} className="text-white" />
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Recursos Principais */}
      <section id="recursos" className="py-20 bg-gray-50">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 mb-4">
              Tudo que você precisa em um só lugar
            </h2>
            <p className="text-xl text-gray-600">
              Banco digital completo com recursos exclusivos
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="bg-white p-8 rounded-2xl shadow-lg hover:shadow-xl transition">
              <div className="w-14 h-14 bg-petroleo-100 rounded-full flex items-center justify-center mb-6">
                <CreditCard className="text-petroleo-700" size={28} />
              </div>
              <h3 className="text-2xl font-bold text-gray-900 mb-3">
                Conta Digital Completa
              </h3>
              <p className="text-gray-600 mb-4">
                Depósitos, saques, transferências e PIX instantâneo. Tudo grátis e sem taxas.
              </p>
              <ul className="space-y-2">
                <li className="flex items-center gap-2 text-sm text-gray-600">
                  <CheckCircle size={16} className="text-green-600" />
                  Transferências ilimitadas
                </li>
                <li className="flex items-center gap-2 text-sm text-gray-600">
                  <CheckCircle size={16} className="text-green-600" />
                  PIX instantâneo
                </li>
                <li className="flex items-center gap-2 text-sm text-gray-600">
                  <CheckCircle size={16} className="text-green-600" />
                  Extrato completo em PDF
                </li>
              </ul>
            </div>

            <div className="bg-white p-8 rounded-2xl shadow-lg hover:shadow-xl transition">
              <div className="w-14 h-14 bg-petroleo-100 rounded-full flex items-center justify-center mb-6">
                <ShoppingBag className="text-petroleo-700" size={28} />
              </div>
              <h3 className="text-2xl font-bold text-gray-900 mb-3">
                Marketplace Integrado
              </h3>
              <p className="text-gray-600 mb-4">
                Compre produtos direto da plataforma com pagamento instantâneo da sua conta.
              </p>
              <ul className="space-y-2">
                <li className="flex items-center gap-2 text-sm text-gray-600">
                  <CheckCircle size={16} className="text-green-600" />
                  Pagamento direto do saldo
                </li>
                <li className="flex items-center gap-2 text-sm text-gray-600">
                  <CheckCircle size={16} className="text-green-600" />
                  Controle de pedidos
                </li>
                <li className="flex items-center gap-2 text-sm text-gray-600">
                  <CheckCircle size={16} className="text-green-600" />
                  Comprovantes digitais
                </li>
              </ul>
            </div>

            <div className="bg-white p-8 rounded-2xl shadow-lg hover:shadow-xl transition">
              <div className="w-14 h-14 bg-yellow-100 rounded-full flex items-center justify-center mb-6">
                <Gift className="text-yellow-600" size={28} />
              </div>
              <h3 className="text-2xl font-bold text-gray-900 mb-3">
                Sistema de Recompensas
              </h3>
              <p className="text-gray-600 mb-4">
                Ganhe moedas e XP a cada compra. Suba de nível e desbloqueie benefícios exclusivos!
              </p>
              <ul className="space-y-2">
                <li className="flex items-center gap-2 text-sm text-gray-600">
                  <CheckCircle size={16} className="text-green-600" />
                  MarketCoins em cada compra
                </li>
                <li className="flex items-center gap-2 text-sm text-gray-600">
                  <CheckCircle size={16} className="text-green-600" />
                  4 níveis de recompensas
                </li>
                <li className="flex items-center gap-2 text-sm text-gray-600">
                  <CheckCircle size={16} className="text-green-600" />
                  Descontos exclusivos
                </li>
              </ul>
            </div>
          </div>
        </div>
      </section>

      {/* Sistema de Níveis */}
      <section className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center mb-16">
            <div className="inline-flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-yellow-100 to-orange-100 text-orange-700 rounded-full text-sm font-semibold mb-4">
              <Sparkles size={16} />
              Sistema de Níveis
            </div>
            <h2 className="text-4xl font-bold text-gray-900 mb-4">
              Quanto mais você compra, mais você ganha
            </h2>
            <p className="text-xl text-gray-600">
              Suba de nível e desbloqueie recompensas incríveis
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
            {[
              {
                nivel: 'Bronze',
                cor: '#CD7F32',
                xp: '0 XP',
                moedas: '1%',
                desconto: '0%',
              },
              {
                nivel: 'Prata',
                cor: '#C0C0C0',
                xp: '1.000 XP',
                moedas: '2%',
                desconto: '5%',
              },
              {
                nivel: 'Ouro',
                cor: '#FFD700',
                xp: '5.000 XP',
                moedas: '3%',
                desconto: '10%',
              },
              {
                nivel: 'Diamante',
                cor: '#B9F2FF',
                xp: '15.000 XP',
                moedas: '5%',
                desconto: '15%',
              },
            ].map((item, index) => (
              <div
                key={index}
                className="relative bg-white border-2 rounded-2xl p-6 hover:shadow-xl transition"
                style={{ borderColor: item.cor }}
              >
                <div
                  className="w-12 h-12 rounded-full flex items-center justify-center mb-4 mx-auto"
                  style={{ backgroundColor: `${item.cor}33` }}
                >
                  <Award size={24} style={{ color: item.cor }} />
                </div>
                <h3 className="text-xl font-bold text-center mb-2" style={{ color: item.cor }}>
                  {item.nivel}
                </h3>
                <p className="text-sm text-gray-600 text-center mb-4">{item.xp}</p>
                <div className="space-y-2">
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-gray-600">MarketCoins:</span>
                    <span className="font-bold text-gray-800">{item.moedas}</span>
                  </div>
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-gray-600">Desconto:</span>
                    <span className="font-bold text-gray-800">{item.desconto}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Recursos Bancários */}
      <section className="py-20 bg-gradient-to-br from-petroleo-900 to-petroleo-700 text-white">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold mb-4">
              Recursos Bancários Completos
            </h2>
            <p className="text-xl opacity-90">
              Tudo que você precisa para gerenciar suas finanças
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="text-center">
              <div className="w-16 h-16 bg-white/20 backdrop-blur rounded-full flex items-center justify-center mx-auto mb-4">
                <Zap size={32} />
              </div>
              <h3 className="text-xl font-bold mb-2">PIX Instantâneo</h3>
              <p className="opacity-90">
                Transferências em tempo real, 24/7, sem taxas
              </p>
            </div>

            <div className="text-center">
              <div className="w-16 h-16 bg-white/20 backdrop-blur rounded-full flex items-center justify-center mx-auto mb-4">
                <Shield size={32} />
              </div>
              <h3 className="text-xl font-bold mb-2">100% Seguro</h3>
              <p className="opacity-90">
                Proteção total em todas as transações
              </p>
            </div>

            <div className="text-center">
              <div className="w-16 h-16 bg-white/20 backdrop-blur rounded-full flex items-center justify-center mx-auto mb-4">
                <TrendingUp size={32} />
              </div>
              <h3 className="text-xl font-bold mb-2">Sem Taxas</h3>
              <p className="opacity-90">
                Todas as operações são totalmente gratuitas
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Final */}
      <section className="py-20 bg-white">
        <div className="max-w-4xl mx-auto px-6 text-center">
          <h2 className="text-5xl font-bold text-gray-900 mb-6">
            Pronto para começar?
          </h2>
          <p className="text-xl text-gray-600 mb-8">
            Crie sua conta grátis e comece a aproveitar todos os benefícios agora mesmo!
          </p>
          <button
            onClick={() => navigate('/cadastro')}
            className="px-12 py-5 bg-gradient-to-r from-petroleo-900 to-petroleo-700 text-white rounded-lg text-lg font-semibold hover:shadow-2xl transition transform hover:scale-105 inline-flex items-center gap-3"
          >
            Criar Conta Grátis
            <ArrowRight size={24} />
          </button>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 text-white py-12">
        <div className="max-w-7xl mx-auto px-6 text-center">
          <h3 className="text-2xl font-bold mb-4">MarketPay</h3>
          <p className="text-gray-400 mb-6">
            Seu banco digital integrado com marketplace
          </p>
          <p className="text-sm text-gray-500">
            © 2026 MarketPay. Todos os direitos reservados.
          </p>
        </div>
      </footer>
    </div>
  );
};
