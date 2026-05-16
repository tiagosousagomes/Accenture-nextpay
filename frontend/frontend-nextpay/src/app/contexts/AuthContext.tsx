import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { Cliente, UserRole } from '../types';

interface AuthContextType {
  user: Cliente | null;
  isAuthenticated: boolean;
  isShopMode: boolean;
  login: (email: string, senha: string) => Promise<boolean>;
  register: (data: Omit<RegisterData, 'role'>) => Promise<boolean>;
  logout: () => void;
  updateProfile: (data: Partial<Cliente>) => void;
  toggleShopMode: () => void;
  isLoading: boolean;
}

interface RegisterData {
  nome: string;
  sobrenome: string;
  cpf: string;
  endereco: string;
  cep?: string;
  bairro?: string;
  cidade?: string;
  estado?: string;
  email: string;
  senha: string;
  fotoPerfil?: string;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<Cliente | null>(null);
  const [isShopMode, setIsShopMode] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
  const storedUser = localStorage.getItem('marketpay_user');

  if (storedUser) {
    try {
      const parsedUser = JSON.parse(storedUser);

      if (parsedUser) {
        parsedUser.id = (parsedUser.id || parsedUser.usuarioId)?.toString();
        setUser(parsedUser);
      }
    } catch (error) {
      localStorage.removeItem('marketpay_user');
    }
  }

  setIsLoading(false);
}, []);

  const login = async (email: string, senha: string): Promise<boolean> => {
    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, senha }),
      });

      if (response.ok) {
        const userData = await response.json();
        
  
        if (userData) {
          userData.id = (userData.id || userData.usuarioId)?.toString();
        }

        setUser(userData);
        localStorage.setItem('marketpay_user', JSON.stringify(userData));
        return true;
      }
      return false;
    } catch (error) {
      console.error('Erro ao realizar login:', error);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (data: RegisterData): Promise<boolean> => {
    const users = JSON.parse(localStorage.getItem('marketpay_users') || '[]');

    if (users.some((u: any) => u.email === data.email || u.cpf === data.cpf)) {
      return false;
    }

    const newUser = {
      id: `user_${Date.now()}`,
      ...data,
      role: 'user' as UserRole,
      createdAt: new Date().toISOString(),
    };

    users.push(newUser);
    localStorage.setItem('marketpay_users', JSON.stringify(users));

    const contas = JSON.parse(localStorage.getItem('marketpay_contas') || '[]');
    const novaConta = {
      id: `conta_${Date.now()}`,
      numero: `${Math.floor(100000 + Math.random() * 900000)}-${Math.floor(10 + Math.random() * 90)}`,
      saldo: 0,
      dataCriacao: new Date().toISOString(),
      clienteId: newUser.id,
    };
    contas.push(novaConta);
    localStorage.setItem('marketpay_contas', JSON.stringify(contas));

    return true;
  };

  const logout = () => {
    setUser(null);
    setIsShopMode(false);
    localStorage.removeItem('marketpay_user');
  };

  const toggleShopMode = () => {
    setIsShopMode(prev => !prev);
  };

  const updateProfile = (data: Partial<Cliente>) => {
    if (!user) return;

    const updatedUser = { ...user, ...data };
    setUser(updatedUser);
    localStorage.setItem('marketpay_user', JSON.stringify(updatedUser));

    const users = JSON.parse(localStorage.getItem('marketpay_users') || '[]');
    const updatedUsers = users.map((u: any) =>
      u.id === user.id ? { ...u, ...data } : u
    );
    localStorage.setItem('marketpay_users', JSON.stringify(updatedUsers));
  };

  return (
    <AuthContext.Provider value={{
      user,
      isAuthenticated: !!user,
      isShopMode,
      login,
      register,
      logout,
      updateProfile,
      toggleShopMode,
      isLoading,
    }}>
      {children}
    </AuthContext.Provider>
  );
};
