import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { RewardsData, calcularNivel, NIVEIS } from '../types/rewards';
import { useAuth } from './AuthContext';

interface RewardsContextType {
  rewards: RewardsData | null;
  adicionarMoedas: (quantidade: number) => void;
  usarMoedas: (quantidade: number) => boolean;
  adicionarTransacao: (tipo: 'compra' | 'venda', valor: number) => void;
  calcularRecompensaCompra: (valorCompra: number) => number;
  calcularDescontoNivel: () => number;
}

const RewardsContext = createContext<RewardsContextType | undefined>(undefined);

export const useRewards = () => {
  const context = useContext(RewardsContext);
  if (!context) {
    throw new Error('useRewards must be used within RewardsProvider');
  }
  return context;
};

export const RewardsProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { user } = useAuth();
  const [rewards, setRewards] = useState<RewardsData | null>(null);

  useEffect(() => {
    if (!user) {
      setRewards(null);
      return;
    }

    const allRewards = JSON.parse(localStorage.getItem('marketpay_rewards') || '{}');
    const userRewards = allRewards[user.id] || {
      moedas: 0,
      totalTransacoes: 0,
      nivel: 'bronze',
      totalGasto: 0,
      totalVendido: 0,
    };

    setRewards(userRewards);
  }, [user]);

  const saveRewards = (newRewards: RewardsData) => {
    if (!user) return;

    const allRewards = JSON.parse(localStorage.getItem('marketpay_rewards') || '{}');
    allRewards[user.id] = newRewards;
    localStorage.setItem('marketpay_rewards', JSON.stringify(allRewards));
    setRewards(newRewards);
  };

  const adicionarMoedas = (quantidade: number) => {
    if (!rewards) return;

    const newRewards = {
      ...rewards,
      moedas: rewards.moedas + quantidade,
    };

    saveRewards(newRewards);
  };

  const usarMoedas = (quantidade: number): boolean => {
    if (!rewards || rewards.moedas < quantidade) return false;

    const newRewards = {
      ...rewards,
      moedas: rewards.moedas - quantidade,
    };

    saveRewards(newRewards);
    return true;
  };

  const adicionarTransacao = (tipo: 'compra' | 'venda', valor: number) => {
    if (!rewards) return;

    const novoTotal = rewards.totalTransacoes + 1;
    const novoNivel = calcularNivel(novoTotal);

    const newRewards = {
      ...rewards,
      totalTransacoes: novoTotal,
      nivel: novoNivel,
      totalGasto: tipo === 'compra' ? rewards.totalGasto + valor : rewards.totalGasto,
      totalVendido: tipo === 'venda' ? rewards.totalVendido + valor : rewards.totalVendido,
    };

    saveRewards(newRewards);
  };

  const calcularRecompensaCompra = (valorCompra: number): number => {
    if (!rewards) return 0;

    const nivelConfig = NIVEIS[rewards.nivel];
    const recompensa = Math.floor(valorCompra * (nivelConfig.recompensaPorcentagem / 100));

    return recompensa;
  };

  const calcularDescontoNivel = (): number => {
    if (!rewards) return 0;

    return NIVEIS[rewards.nivel].desconto;
  };

  return (
    <RewardsContext.Provider value={{
      rewards,
      adicionarMoedas,
      usarMoedas,
      adicionarTransacao,
      calcularRecompensaCompra,
      calcularDescontoNivel,
    }}>
      {children}
    </RewardsContext.Provider>
  );
};
