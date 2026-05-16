import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { ContaCorrente, Transacao } from '../types';
import { useAuth } from './AuthContext';
import { atualizarSistema } from '../utils/eventBus';

interface BankContextType {
  conta: ContaCorrente | null;
  saldoMoedas: number;
  transacoes: Transacao[];
  comprasEVendas: Transacao[];
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
  const [saldoMoedas, setSaldoMoedas] = useState<number>(0);
  const [transacoes, setTransacoes] = useState<Transacao[]>([]);
  const [comprasEVendas, setComprasEVendas] = useState<Transacao[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const loadConta = async () => {
    if (!user) {
      setConta(null);
      setTransacoes([]);
      setComprasEVendas([]);
      return;
    }

    setIsLoading(true);
    try {
      const currentUserId = user.id || (user as any).usuarioId;
      if (!currentUserId) return;

      const response = await fetch(`http://localhost:8080/api/usuarios/${currentUserId}`);
      if (response.ok) {
        const fullUserData = await response.json();

        // Mapear os dados do backend para o formato esperado pelo frontend
        if (fullUserData.conta) {
          const mappedConta: ContaCorrente = {
            id: fullUserData.conta.id.toString(),
            numero: fullUserData.conta.numeroConta,
            saldo: fullUserData.conta.saldo,
            dataCriacao: new Date().toISOString(), // O backend não parece retornar a data de criação, usamos a atual ou mantemos opcional
            clienteId: fullUserData.id.toString()
          };
          setConta(mappedConta);
        }
        setSaldoMoedas(fullUserData.pontuacao || 0);

        // Buscar transações do backend
        const transResponse = await fetch(`http://localhost:8080/api/transacoes/usuario/${currentUserId}`);
        if (transResponse.ok) {
          const transData = await transResponse.json();
          // Mapear transações do backend para o formato do frontend
          const mappedTransacoes: Transacao[] = transData.map((t: any) => ({
            id: t.id.toString(),
            tipo: t.tipo.toLowerCase() as any,
            valor: t.valor,
            data: t.data,
            descricao: t.descricao,
            contaId: t.usuario.id.toString(),
            status: 'sucesso'
          }));
          setTransacoes(mappedTransacoes.sort((a, b) => new Date(b.data).getTime() - new Date(a.data).getTime()));
          setComprasEVendas(mappedTransacoes.filter((t) => t.tipo === 'compra' || t.tipo === 'venda'));
        } else {
          setTransacoes([]);
        }
      }
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
    const atualizar = () => {
      refreshConta();
    };

    window.addEventListener('atualizar-sistema', atualizar);

    return () => {
      window.removeEventListener('atualizar-sistema', atualizar);
    };
  }, [user]);

  const refreshConta = () => {
    loadConta();
  };

  const depositar = async (valor: number): Promise<boolean> => {
    if (!user || valor <= 0) return false;

    setIsLoading(true);
    try {
      // Tenta obter o ID de várias fontes possíveis
      const currentUserId = user.id || (user as any).usuarioId;

      if (!currentUserId) {
        console.error('BankContext: Nenhum ID encontrado no objeto de usuário!', user);
        return false;
      }

      const payload = {
        usuarioId: currentUserId,
        usuarioOrigemId: currentUserId,
        usuarioDestinoId: currentUserId,
        valor: valor
      };

      const response = await fetch('http://localhost:8080/api/carteiras/deposito', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        refreshConta();
        atualizarSistema();
        return true;
      }
      return false;
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
      const currentUserId = user.id || (user as any).usuarioId;

      const payload = {
        usuarioId: currentUserId,
        usuarioOrigemId: currentUserId,
        usuarioDestinoId: currentUserId,
        valor: valor
      };

      const response = await fetch('http://localhost:8080/api/carteiras/saque', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        refreshConta();
        atualizarSistema();
        return true;
      }
      return false;
    } catch (error) {
      console.error('Erro no saque:', error);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const transferir = async (contaDestinoNumero: string, valor: number): Promise<boolean> => {
    if (!user || !conta || valor <= 0 || conta.saldo < valor) return false;

    setIsLoading(true);
    try {
      const currentUserId = user.id || (user as any).usuarioId;

      const payload = {
        usuarioId: currentUserId,
        usuarioOrigemId: currentUserId,
        usuarioDestinoId: contaDestinoNumero,
        valor: valor
      };

      const response = await fetch('http://localhost:8080/api/carteiras/transferencia', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        refreshConta();
        atualizarSistema();
        return true;
      }
      return false;
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
      const currentUserId = user.id || (user as any).usuarioId;

      const payload = {
        usuarioId: currentUserId,
        usuarioOrigemId: currentUserId,
        chavePix: chavePix,
        valor: valor
      };

      const response = await fetch('http://localhost:8080/api/carteiras/pix', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        refreshConta();
        atualizarSistema();
        return true;
      }
      return false;
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
    <BankContext.Provider value={{
      conta,
      saldoMoedas,
      transacoes,
      comprasEVendas,
      depositar,
      sacar,
      transferir,
      pix,
      pagarPedido,
      refreshConta,
      isLoading,
    }}>
      {children}
    </BankContext.Provider>
  );
};
