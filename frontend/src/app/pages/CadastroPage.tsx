import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import { UserRole } from '../types';

export const CadastroPage: React.FC = () => {
  const [formData, setFormData] = useState({
    nome: '',
    sobrenome: '',
    cpf: '',
    email: '',
    senha: '',
    confirmarSenha: '',
    // Endereço
    cep: '',
    logradouro: '',
    numero: '',
    complemento: '',
    bairro: '',
    localidade: '',
    uf: '',
    // UI Only
    fotoPerfil: '',
  });
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const buscarCep = async (cep: string) => {
    const cepLimpo = cep.replace(/\D/g, '');
    if (cepLimpo.length === 8) {
      try {
        const res = await fetch(`https://viacep.com.br/ws/${cepLimpo}/json/`);
        const data = await res.json();
        if (!data.erro) {
          setFormData(prev => ({
            ...prev,
            logradouro: data.logradouro || '',
            bairro: data.bairro || '',
            localidade: data.localidade || '',
            uf: data.uf || '',
          }));
          toast.success('Endereço encontrado com sucesso!');
        } else {
          toast.error('CEP não encontrado');
        }
      } catch (err) {
        console.error('Erro ao buscar CEP', err);
        toast.error('Erro ao buscar CEP');
      }
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleCepChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setFormData(prev => ({ ...prev, cep: value }));
  };

  const handleCepBlur = () => {
    if (formData.cep.replace(/\D/g, '').length === 8) {
      buscarCep(formData.cep);
    }
  };

  const handleFotoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setFormData(prev => ({ ...prev, fotoPerfil: reader.result as string }));
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.nome) {
      toast.error('Preencha o nome obrigatório');
      return;
    }

    if (!formData.sobrenome) {
      toast.error('Preencha o sobrenome obrigatório');
      return;
    }

    if (!formData.email) {
      toast.error('Preencha o email obrigatório');
      return;
    }

    if (!formData.cpf) {
      toast.error('Preencha o CPF obrigatório');
      return;
    }

    if (!formData.cep) {
      toast.error('Preencha o CEP obrigatório');
      return;
    }

    if (!formData.logradouro) {
      toast.error('Preencha o logradouro obrigatório');
      return;
    }

    if (!formData.numero) {
      toast.error('Preencha o número obrigatório');
      return;
    }

    if (!formData.bairro) {
      toast.error('Preencha o bairro obrigatório');
      return;
    }

    if (!formData.localidade) {
      toast.error('Preencha a localidade obrigatória');
      return;
    }

    if (!formData.uf) {
      toast.error('Preencha a UF obrigatória');
      return;
    }

    if (!formData.senha) {
      toast.error('Preencha a senha obrigatória');
      return;
    }

    if (!formData.confirmarSenha) {
      toast.error('Preencha a confirmação de senha obrigatória');
      return;
    }

    if (formData.senha !== formData.confirmarSenha) {
      toast.error('As senhas não coincidem');
      return;
    }

    if (formData.cpf.replace(/\D/g, '').length !== 11) {
      toast.error('CPF inválido');
      return;
    }

    const payload = {
      nome: `${formData.nome} ${formData.sobrenome}`.trim(),
      cpfCnpj: formData.cpf.replace(/\D/g, ''),
      email: formData.email,
      senha: formData.senha,
      fotoPerfil: formData.fotoPerfil,
      produtos: [],
      pontuacao: 0,
      nivel: "BRONZE"
    };

    setLoading(true);
    try {
      const response = await fetch(`http://localhost:8080/api/usuarios?cep=${encodeURIComponent(formData.cep)}&numero=${encodeURIComponent(formData.numero)}`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(payload),
        });

      if (response.ok) {
        toast.success('Conta criada com sucesso!');
        navigate('/login');
      } else {
        const errorData = await response.json();
        toast.error(errorData.message || 'Erro ao cadastrar usuário');
      }
    } catch (error) {
      console.error('Erro na requisição:', error);
      toast.error('Erro ao conectar com o servidor');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-petroleo-900 via-petroleo-700 to-petroleo-950 flex items-center justify-center p-4">
      <div className="w-full max-w-3xl my-8">
        <div className="bg-white rounded-2xl shadow-2xl p-8">
          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold bg-gradient-to-r from-petroleo-900 to-petroleo-700 bg-clip-text text-transparent">
              Criar Conta
            </h1>
            <p className="text-gray-600 mt-2">Junte-se ao MarketPay</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">

            {/* Foto de Perfil */}
            <div className="flex flex-col items-center justify-center mb-6">
              <div className="w-24 h-24 rounded-full bg-gray-100 flex items-center justify-center border-2 border-dashed border-gray-300 overflow-hidden mb-2">
                {formData.fotoPerfil ? (
                  <img src={formData.fotoPerfil} alt="Preview" className="w-full h-full object-cover" />
                ) : (
                  <span className="text-xs text-gray-400 text-center">Foto (opcional)</span>
                )}
              </div>
              <label className="cursor-pointer text-sm font-medium text-petroleo-700 hover:text-petroleo-800">
                Escolher imagem
                <input
                  type="file"
                  accept="image/*"
                  onChange={handleFotoChange}
                  className="hidden"
                />
              </label>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Nome *
                </label>
                <input
                  type="text"
                  name="nome"
                  value={formData.nome}
                  onChange={handleChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none transition"
                  placeholder="João"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Sobrenome *
                </label>
                <input
                  type="text"
                  name="sobrenome"
                  value={formData.sobrenome}
                  onChange={handleChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none transition"
                  placeholder="Silva"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  CPF *
                </label>
                <input
                  type="text"
                  name="cpf"
                  value={formData.cpf}
                  onChange={handleChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none transition"
                  placeholder="000.000.000-00"
                  maxLength={14}
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  CEP
                </label>
                <input
                  type="text"
                  name="cep"
                  value={formData.cep}
                  onChange={handleCepChange}
                  onBlur={handleCepBlur}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none transition"
                  placeholder="00000-000"
                  maxLength={9}
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div className="md:col-span-3">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Logradouro
                </label>
                <input
                  type="text"
                  name="logradouro"
                  value={formData.logradouro}
                  onChange={handleChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none transition"
                  placeholder="Rua..."
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Número
                </label>
                <input
                  type="text"
                  name="numero"
                  value={formData.numero}
                  onChange={handleChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none transition"
                  placeholder="123"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Complemento
                </label>
                <input
                  type="text"
                  name="complemento"
                  value={formData.complemento}
                  onChange={handleChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none transition"
                  placeholder="Apto, Bloco..."
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Bairro
                </label>
                <input
                  type="text"
                  name="bairro"
                  value={formData.bairro}
                  onChange={handleChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none transition"
                  placeholder="Bairro"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div className="md:col-span-3">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Cidade
                </label>
                <input
                  type="text"
                  name="localidade"
                  value={formData.localidade}
                  onChange={handleChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none transition"
                  placeholder="Cidade"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  UF
                </label>
                <input
                  type="text"
                  name="uf"
                  value={formData.uf}
                  onChange={handleChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none transition"
                  placeholder="UF"
                  maxLength={2}
                />
              </div>
            </div>

            <div className="grid grid-cols-1 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Email *
                </label>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none transition"
                  placeholder="seu@email.com"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Senha *
                </label>
                <input
                  type="password"
                  name="senha"
                  value={formData.senha}
                  onChange={handleChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none transition"
                  placeholder="••••••••"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Confirmar Senha *
                </label>
                <input
                  type="password"
                  name="confirmarSenha"
                  value={formData.confirmarSenha}
                  onChange={handleChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-petroleo-500 focus:border-transparent outline-none transition"
                  placeholder="••••••••"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className={`w-full bg-gradient-to-r from-petroleo-900 to-petroleo-700 text-white py-3 rounded-lg font-semibold hover:shadow-lg transition transform hover:scale-105 mt-6 flex items-center justify-center gap-2 ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
            >
              {loading ? (
                <>
                  <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Cadastrando...
                </>
              ) : (
                'Criar Conta'
              )}
            </button>
          </form>

          <div className="mt-6 text-center">
            <button
              onClick={() => navigate('/login')}
              className="text-petroleo-700 hover:text-petroleo-800 font-medium"
            >
              Já tem uma conta? Faça login
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
