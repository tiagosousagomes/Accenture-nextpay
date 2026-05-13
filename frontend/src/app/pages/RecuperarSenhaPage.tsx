import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';

export const RecuperarSenhaPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const navigate = useNavigate();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!email) {
      toast.error('Digite seu email');
      return;
    }

    // Simulação de envio de email
    toast.success('Email de recuperação enviado! Verifique sua caixa de entrada.');
    setTimeout(() => {
      navigate('/');
    }, 2000);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-petroleo-900 via-petroleo-700 to-petroleo-950 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-2xl shadow-2xl p-8">
          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold bg-gradient-to-r from-petroleo-900 to-petroleo-700 bg-clip-text text-transparent">
              Recuperar Senha
            </h1>
            <p className="text-gray-600 mt-2">Digite seu email para recuperar o acesso</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Email
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none transition"
                placeholder="seu@email.com"
              />
            </div>

            <button
              type="submit"
              className="w-full bg-gradient-to-r from-petroleo-900 to-petroleo-700 text-white py-3 rounded-lg font-semibold hover:shadow-lg transition transform hover:scale-105"
            >
              Enviar Email de Recuperação
            </button>
          </form>

          <div className="mt-6 text-center">
            <button
              onClick={() => navigate('/')}
              className="text-petroleo-700 hover:text-petroleo-800 font-medium"
            >
              Voltar para o login
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
