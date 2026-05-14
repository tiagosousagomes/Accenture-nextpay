export type NivelTier = 'bronze' | 'prata' | 'ouro' | 'diamante';

export interface RewardsData {
  moedas: number;
  totalTransacoes: number; // Total de compras E vendas
  nivel: NivelTier;
  totalGasto: number;
  totalVendido: number;
}

export interface NivelConfig {
  nome: string;
  cor: string;
  corBg: string;
  transacoesNecessarias: number; // Mudado de XP para transações
  recompensaPorcentagem: number;
  desconto: number;
}

export const NIVEIS: Record<NivelTier, NivelConfig> = {
  bronze: {
    nome: 'Bronze',
    cor: '#CD7F32',
    corBg: '#FFF4E6',
    transacoesNecessarias: 0, // 0-4 transações
    recompensaPorcentagem: 1,
    desconto: 0,
  },
  prata: {
    nome: 'Prata',
    cor: '#C0C0C0',
    corBg: '#F5F5F5',
    transacoesNecessarias: 5, // 5-9 transações
    recompensaPorcentagem: 2,
    desconto: 5,
  },
  ouro: {
    nome: 'Ouro',
    cor: '#FFD700',
    corBg: '#FFFACD',
    transacoesNecessarias: 10, // 10-19 transações
    recompensaPorcentagem: 3,
    desconto: 10,
  },
  diamante: {
    nome: 'Diamante',
    cor: '#B9F2FF',
    corBg: '#E0F7FF',
    transacoesNecessarias: 20, // 20+ transações
    recompensaPorcentagem: 5,
    desconto: 15,
  },
};

export const calcularNivel = (totalTransacoes: number): NivelTier => {
  if (totalTransacoes >= NIVEIS.diamante.transacoesNecessarias) return 'diamante';
  if (totalTransacoes >= NIVEIS.ouro.transacoesNecessarias) return 'ouro';
  if (totalTransacoes >= NIVEIS.prata.transacoesNecessarias) return 'prata';
  return 'bronze';
};

export const calcularProximoNivel = (nivelAtual: NivelTier): NivelTier | null => {
  switch (nivelAtual) {
    case 'bronze': return 'prata';
    case 'prata': return 'ouro';
    case 'ouro': return 'diamante';
    case 'diamante': return null;
  }
};

export const calcularProgressoNivel = (totalTransacoes: number, nivel: NivelTier): number => {
  const proximoNivel = calcularProximoNivel(nivel);
  if (!proximoNivel) return 100;

  const transacoesNivelAtual = NIVEIS[nivel].transacoesNecessarias;
  const transacoesProximoNivel = NIVEIS[proximoNivel].transacoesNecessarias;
  const transacoesProgresso = totalTransacoes - transacoesNivelAtual;
  const transacoesTotal = transacoesProximoNivel - transacoesNivelAtual;

  return Math.min(100, (transacoesProgresso / transacoesTotal) * 100);
};
