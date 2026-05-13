import React, { useState } from 'react';
import { Layout } from '../components/Layout';
import { useBank } from '../contexts/BankContext';
import {
  Wallet,
  ArrowDownCircle,
  ArrowUpCircle,
  Send,
  Zap,
  CreditCard,
} from 'lucide-react';
import { toast } from 'sonner';

interface ModalProps {
  titulo: string;
  children: React.ReactNode;
  onConfirm: () => void;
  onCancel: () => void;
  isLoading: boolean;
}

const Modal: React.FC<ModalProps> = ({ titulo, children, onConfirm, onCancel, isLoading }) => (
  <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
    <div className="bg-white rounded-2xl p-6 w-full max-w-md">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">{titulo}</h2>
      {children}
      <div className="flex gap-3 mt-6">
        <button
          onClick={onCancel}
          disabled={isLoading}
          className="flex-1 px-4 py-3 bg-gray-200 text-gray-700 rounded-lg font-semibold hover:bg-gray-300 transition disabled:opacity-50"
        >
          Cancelar
        </button>
        <button
          onClick={onConfirm}
          disabled={isLoading}
          className="flex-1 px-4 py-3 bg-gradient-to-r from-petroleo-900 to-petroleo-700 text-white rounded-lg font-semibold hover:shadow-lg transition flex items-center justify-center disabled:opacity-70 disabled:cursor-not-allowed"
        >
          {isLoading ? (
            <svg className="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
          ) : 'Confirmar'}
        </button>
      </div>
    </div>
  </div>
);

export const ContaCorrentePage: React.FC = () => {
  const { conta, depositar, sacar, transferir, pix } = useBank();
  const [modalAberto, setModalAberto] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [valor, setValor] = useState('');
  const [contaDestino, setContaDestino] = useState('');
  const [chavePix, setChavePix] = useState('');
  const [destinatarioPix, setDestinatarioPix] = useState<any>(null);
  const [confirmandoPix, setConfirmandoPix] = useState(false);

  const handleDeposito = async () => {
    const valorNum = parseFloat(valor);
    if (isNaN(valorNum) || valorNum <= 0) {
      toast.error('Digite um valor válido');
      return;
    }

    setIsLoading(true);
    try {
      const sucesso = await depositar(valorNum);
      if (sucesso) {
        toast.success(`Depósito de R$ ${valorNum.toFixed(2)} realizado com sucesso!`);
        setModalAberto(null);
        setValor('');
      } else {
        toast.error('Erro ao realizar depósito');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleSaque = async () => {
    const valorNum = parseFloat(valor);
    if (isNaN(valorNum) || valorNum <= 0) {
      toast.error('Digite um valor válido');
      return;
    }

    if (!conta || valorNum > conta.saldo) {
      toast.error('Saldo insuficiente');
      return;
    }

    setIsLoading(true);
    try {
      const sucesso = await sacar(valorNum);
      if (sucesso) {
        toast.success(`Saque de R$ ${valorNum.toFixed(2)} realizado com sucesso!`);
        setModalAberto(null);
        setValor('');
      } else {
        toast.error('Erro ao realizar saque');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleTransferencia = async () => {
    const valorNum = parseFloat(valor);
    if (isNaN(valorNum) || valorNum <= 0) {
      toast.error('Digite um valor válido');
      return;
    }

    if (!contaDestino) {
      toast.error('Digite a conta de destino');
      return;
    }

    if (!conta || valorNum > conta.saldo) {
      toast.error('Saldo insuficiente');
      return;
    }

    setIsLoading(true);
    try {
      const sucesso = await transferir(contaDestino, valorNum);
      if (sucesso) {
        toast.success(`Transferência de R$ ${valorNum.toFixed(2)} realizada com sucesso!`);
        setModalAberto(null);
        setValor('');
        setContaDestino('');
      } else {
        toast.error('Erro na transferência. Verifique os dados.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handlePix = async (): Promise<boolean> => {
    const valorNum = parseFloat(valor);
    if (isNaN(valorNum) || valorNum <= 0) {
      toast.error('Digite um valor válido');
      return false;
    }

    if (!chavePix) {
      toast.error('Digite a chave PIX');
      return false;;
    }

    if (!conta || valorNum > conta.saldo) {
      toast.error('Saldo insuficiente');
      return false;;
    }

    setIsLoading(true);
    try {
      const sucesso = await pix(chavePix, valorNum);
      if (sucesso) {
          toast.success(`PIX de R$ ${valorNum.toFixed(2)} enviado com sucesso!`);
          setModalAberto(null);
          setValor('');
          setChavePix('');
          return true;

        } else {
          toast.error('Erro ao enviar PIX');
          return false;      
        }
    }catch (error) {
      toast.error('Erro ao enviar PIX');
      return false;

    } finally {
      setIsLoading(false);
    }
  };

  const closeModals = () => {
    setModalAberto(null);
    setValor('');
    setContaDestino('');
    setChavePix('');
    setDestinatarioPix(null);
    setConfirmandoPix(false);
  };

  const buscarChavePix = async () => {
    try {

      const response = await fetch(
        `http://localhost:8080/api/carteiras/pix/buscar?chave=${chavePix}`
      );

      if (!response.ok) {
        toast.error('Chave PIX não encontrada');

        setDestinatarioPix(null);
        setConfirmandoPix(false);

        return;
      }

      const data = await response.json();

      setDestinatarioPix(data);
      setConfirmandoPix(true);

    } catch (error) {
      toast.error('Erro ao buscar chave PIX');
    }
};

  return (
    <Layout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-800">Conta Corrente</h1>
          <p className="text-gray-600 mt-1">Gerencie suas operações bancárias</p>
        </div>

        {/* Informações da Conta */}
        <div className="bg-gradient-to-r from-petroleo-900 to-petroleo-700 rounded-2xl p-8 text-white shadow-xl">
          <div className="flex items-center gap-3 mb-6">
            <CreditCard size={32} />
            <div>
              <p className="text-sm text-petroleo-100">Conta Corrente</p>
              <p className="text-lg font-semibold">{conta?.numero}</p>
            </div>
          </div>

          <div className="mb-2">
            <p className="text-sm text-petroleo-100">Saldo Disponível</p>
            <p className="text-4xl font-bold">R$ {conta?.saldo.toFixed(2) || '0.00'}</p>
          </div>

          <p className="text-sm text-petroleo-100 mt-4">
            Conta criada em {conta ? new Date(conta.dataCriacao).toLocaleDateString('pt-BR') : ''}
          </p>
        </div>

        {/* Operações */}
        <div>
          <h2 className="text-xl font-semibold text-gray-800 mb-4">Operações</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 max-w-4xl mx-auto">
            <button
              onClick={() => setModalAberto('deposito')}
              className="bg-white p-6 rounded-xl shadow-md hover:shadow-lg transition group"
            >
              <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center mb-4 group-hover:scale-110 transition">
                <ArrowDownCircle className="text-green-600" size={24} />
              </div>
              <h3 className="font-semibold text-gray-800">Depositar</h3>
              <p className="text-sm text-gray-600 mt-1">Adicionar dinheiro à conta</p>
            </button>

            <button
              onClick={() => setModalAberto('saque')}
              className="bg-white p-6 rounded-xl shadow-md hover:shadow-lg transition group"
            >
              <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center mb-4 group-hover:scale-110 transition">
                <ArrowUpCircle className="text-red-600" size={24} />
              </div>
              <h3 className="font-semibold text-gray-800">Sacar</h3>
              <p className="text-sm text-gray-600 mt-1">Retirar dinheiro da conta</p>
            </button>
            
            <button
              onClick={() => setModalAberto('pix')}
              className="bg-white p-6 rounded-xl shadow-md hover:shadow-lg transition group"
            >
              <div className="w-12 h-12 bg-petroleo-100 rounded-full flex items-center justify-center mb-4 group-hover:scale-110 transition">
                <Zap className="text-petroleo-700" size={24} />
              </div>
              <h3 className="font-semibold text-gray-800">Transferência PIX</h3>
              <p className="text-sm text-gray-600 mt-1"> Transferência por CPF ou E-mail</p>
            </button>
          </div>
        </div>

        {/* Modais */}
        {modalAberto === 'deposito' && (
          <Modal titulo="Depositar" onConfirm={handleDeposito} onCancel={closeModals} isLoading={isLoading}>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Valor do Depósito
              </label>
              <input
                type="number"
                value={valor}
                onChange={(e) => setValor(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none"
                placeholder="0.00"
                step="0.01"
                min="0"
              />
            </div>
          </Modal>
        )}

        {modalAberto === 'saque' && (
          <Modal titulo="Sacar" onConfirm={handleSaque} onCancel={closeModals} isLoading={isLoading}>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Valor do Saque
              </label>
              <input
                type="number"
                value={valor}
                onChange={(e) => setValor(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none"
                placeholder="0.00"
                step="0.01"
                min="0"
              />
              <p className="text-sm text-gray-600 mt-2">
                Saldo disponível: R$ {conta?.saldo.toFixed(2)}
              </p>
            </div>
          </Modal>
        )}

        {modalAberto === 'transferencia' && (
          <Modal titulo="Transferir" onConfirm={handleTransferencia} onCancel={closeModals} isLoading={isLoading}>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Conta de Destino
                </label>
                <input
                  type="text"
                  value={contaDestino}
                  onChange={(e) => setContaDestino(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none"
                  placeholder="000000-00"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Valor da Transferência
                </label>
                <input
                  type="number"
                  value={valor}
                  onChange={(e) => setValor(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none"
                  placeholder="0.00"
                  step="0.01"
                  min="0"
                />
                <p className="text-sm text-gray-600 mt-2">
                  Saldo disponível: R$ {conta?.saldo.toFixed(2)}
                </p>
              </div>
            </div>
          </Modal>
        )}

        {modalAberto === 'pix' && (
          <Modal 
          titulo={
            confirmandoPix
              ? 'Confirmar Transferência PIX'
              : 'Transferência PIX'
          } 
          onConfirm={async () => {
              if (!confirmandoPix) {
                await buscarChavePix();
                return;
              }

              const sucesso = await handlePix();

              if (sucesso) {

                toast.success('PIX enviado com sucesso');

                setModalAberto(null);

                setChavePix('');
                setValor('');

                setDestinatarioPix(null);
                setConfirmandoPix(false);

              } else {

                toast.error('Erro ao enviar PIX');

              }
            }}
          onCancel={closeModals} 
          isLoading={isLoading}
          >
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Chave PIX
                </label>
                <input
                  type="text"
                  value={chavePix}
                  onChange={(e) => {
                    setChavePix(e.target.value);
                    setDestinatarioPix(null);
                    setConfirmandoPix(false);
                  }}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none"
                  placeholder="Email, CPF, Telefone ou Chave Aleatória"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Valor do PIX
                </label>
                <input
                  type="number"
                  value={valor}
                  onChange={(e) => setValor(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none"
                  placeholder="0.00"
                  step="0.01"
                  min="0"
                />
                <p className="text-sm text-gray-600 mt-2">
                  Saldo disponível: R$ {conta?.saldo.toFixed(2)}
                </p>
                {destinatarioPix && (
                  <div className="bg-green-50 border border-green-200 rounded-lg p-3 mt-3">
                    
                    <p className="text-sm text-gray-600">
                      Destinatário encontrado:
                    </p>

                    <p className="font-bold text-green-700">
                      {destinatarioPix.nome}
                    </p>

                    <p className="text-xs text-gray-500">
                      {destinatarioPix.email}
                    </p>

                  </div>
                )}
              </div>
            </div>
          </Modal>
        )}
      </div>
    </Layout>
  );
};
