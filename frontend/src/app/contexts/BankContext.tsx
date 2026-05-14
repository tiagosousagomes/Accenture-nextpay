import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { ContaCorrente, Transacao } from '../types';
import { useAuth } from './AuthContext';
import api from '../services/api';

interface BankContextType {
  conta: ContaCorrente | null;
  transacoes: Transacao[];
  depositar: (valor: number) => Promise<boolean>;
  sacar: (valor: number) => Promise<boolean>;
  transferir: (contaDestinoNumero: string, valor: number) => Promise<boolean>;
  pix: (chavePix: string, valor: number) => Promise<boolean>;
  pagarPedido: (pedidoId: string, valor: number, tipo: string) => Promise<boolean>;
  refreshConta: () => void;
  isLoading: boolean;
}

const BankContext = createContext<BankContextType | undefined>(undefined);

export const useBank = () => {
  const context = useContext(BankContext);
  if (!context) {
    throw new Error('useBank must be used within BankProvider');
  }
  return context;
};

export const BankProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { user } = useAuth();
  const [conta, setConta] = useState<ContaCorrente | null>(null);
  const [transacoes, setTransacoes] = useState<Transacao[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const loadConta = async () => {
    if (!user) {
      setConta(null);
      setTransacoes([]);
      return;
    }

    setIsLoading(true);
    try {
      const currentUserId = user.id || (user as any).usuarioId;
      if (!currentUserId) return;

      const userResponse = await api.get(`/api/usuarios/${currentUserId}`);
      const fullUserData = userResponse.data;

      if (fullUserData.conta) {
        const mappedConta: ContaCorrente = {
          id: fullUserData.conta.id.toString(),
          numero: fullUserData.conta.numeroConta,
          saldo: fullUserData.conta.saldo,
          dataCriacao: new Date().toISOString(),
          clienteId: fullUserData.id.toString(),
        };
        setConta(mappedConta);
      }

      const transResponse = await api.get('/api/transacoes');
      const transData = transResponse.data;
      const mappedTransacoes: Transacao[] = transData.map((t: any) => ({
        id: t.id.toString(),
        tipo: t.tipo.toLowerCase() as any,
        valor: t.valor,
        data: t.data,
        descricao: t.descricao,
        contaId: t.usuario.id.toString(),
        status: 'sucesso',
      }));
      setTransacoes(
        mappedTransacoes.sort((a, b) => new Date(b.data).getTime() - new Date(a.data).getTime())
      );
    } catch (error) {
      console.error('Erro ao carregar dados da conta:', error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadConta();
  }, [user]);

  useEffect(() => {
    const atualizar = () => loadConta();
    window.addEventListener('atualizar-conta', atualizar);
    return () => window.removeEventListener('atualizar-conta', atualizar);
  }, [user]);

  const refreshConta = () => loadConta();

  const depositar = async (valor: number): Promise<boolean> => {
    if (!user || valor <= 0) return false;

    setIsLoading(true);
    try {
      await api.post('/api/carteiras/deposito', { valor });
      refreshConta();
      return true;
    } catch (error) {
      console.error('Erro no depósito:', error);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const sacar = async (valor: number): Promise<boolean> => {
    if (!user || !conta || valor <= 0 || conta.saldo < valor) return false;

    setIsLoading(true);
    try {
      await api.post('/api/carteiras/saque', { valor });
      refreshConta();
      return true;
    } catch (error) {
      console.error('Erro no saque:', error);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const transferir = async (usuarioDestinoId: string, valor: number): Promise<boolean> => {
    if (!user || !conta || valor <= 0 || conta.saldo < valor) return false;

    setIsLoading(true);
    try {
      await api.post('/api/carteiras/transferencia', { usuarioDestinoId, valor });
      refreshConta();
      return true;
    } catch (error) {
      console.error('Erro na transferência:', error);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const pix = async (chavePix: string, valor: number): Promise<boolean> => {
    if (!user || !conta || valor <= 0 || conta.saldo < valor) return false;

    setIsLoading(true);
    try {
      await api.post('/api/carteiras/pix', { chavePix, valor });
      refreshConta();
      return true;
    } catch (error) {
      console.error('Erro no PIX:', error);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const pagarPedido = async (vendedorId: string, valor: number, tipo: string): Promise<boolean> => {
    if (!user || !conta || valor <= 0 || conta.saldo < valor) return false;
    try {
      if (tipo === 'pix') {
        return await pix(vendedorId, valor);
      }
      return await transferir(vendedorId, valor);
    } catch (error) {
      console.error('Erro ao pagar pedido:', error);
      return false;
    }
  };

  return (
    <BankContext.Provider
      value={{
        conta,
        transacoes,
        depositar,
        sacar,
        transferir,
        pix,
        pagarPedido,
        refreshConta,
        isLoading,
      }}
    >
      {children}
    </BankContext.Provider>
  );
};
