import React, { ReactNode } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useMarketplace } from '../contexts/MarketplaceContext';
import { useBank } from '../contexts/BankContext';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  Home,
  CreditCard,
  Receipt,
  ShoppingCart,
  Package,
  BarChart3,
  User,
  LogOut,
  Store,
  Gift,
} from 'lucide-react';
import { VirtualAssistant } from './VirtualAssistant';

interface LayoutProps {
  children: ReactNode;
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {
  const { user, logout, isShopMode, toggleShopMode, isLoading: authLoading } = useAuth();
  const { getProdutosByVendedor, isLoading: marketplaceLoading } = useMarketplace();
  const { isLoading: bankLoading } = useBank();
  const navigate = useNavigate();
  const location = useLocation();

  const isGlobalLoading = authLoading || marketplaceLoading || bankLoading;

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const menuItemsCliente = [
    { icon: Home, label: 'Dashboard', path: '/dashboard' },
    { icon: CreditCard, label: 'Conta Corrente', path: '/conta' },
    { icon: Receipt, label: 'Extrato', path: '/extrato' },
    { icon: ShoppingCart, label: 'Marketplace', path: '/marketplace' },
    { icon: Package, label: 'Meus Pedidos', path: '/pedidos' },
    { icon: Gift, label: 'Recompensas', path: '/recompensas' },
    { icon: User, label: 'Perfil', path: '/perfil' },
  ];

  const menuItemsLoja = [
    { icon: Home, label: 'Dashboard', path: '/dashboard' },
    { icon: CreditCard, label: 'Conta Corrente', path: '/conta' },
    { icon: Receipt, label: 'Extrato', path: '/extrato' },
    { icon: Store, label: 'Gestão de Produtos', path: '/produtos' },
    { icon: Package, label: 'Pedidos', path: '/pedidos-loja' },
    { icon: BarChart3, label: 'Relatórios', path: '/relatorios' },
    { icon: User, label: 'Perfil', path: '/perfil' },
  ];

  const menuItems = isShopMode ? menuItemsLoja : menuItemsCliente;

  return (
    <div className="min-h-screen bg-gray-50 flex">
      {/* Sidebar */}
      <aside className="w-64 bg-gradient-to-b from-petroleo-900 to-petroleo-950 text-white fixed h-full">
        <div className="p-6">
          <h1 className="text-2xl font-bold">MarketPay</h1>
          <p className="text-sm text-petroleo-100 mt-1">
            {isShopMode ? 'Painel do Gerente' : 'Banco Digital'}
          </p>
        </div>

        { (isShopMode || getProdutosByVendedor(user?.id || '').length > 0) && (
          <div className="px-6 mb-4">
            <button
              onClick={toggleShopMode}
              className={`w-full flex items-center justify-center gap-2 py-2 rounded-lg text-sm font-medium transition-all ${isShopMode
                ? 'bg-amber-500 hover:bg-amber-600 text-white shadow-lg'
                : 'bg-white/10 hover:bg-white/20 text-white'
                }`}
            >
              <Store size={16} />
              {isShopMode ? 'Mudar para Compras' : 'Mudar para Loja'}
            </button>
          </div>
        )}

        <nav className="mt-6">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.path;

            return (
              <button
                key={item.path}
                onClick={() => navigate(item.path)}
                className={`w-full flex items-center gap-3 px-6 py-3 transition ${isActive
                  ? 'bg-white/20 border-l-4 border-white'
                  : 'hover:bg-white/10'
                  }`}
              >
                <Icon size={20} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>

        <div className="absolute bottom-0 w-64 p-6 border-t border-white/20">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-10 h-10 bg-white/20 rounded-full flex items-center justify-center">
              <User size={20} />
            </div>
            <div className="flex-1">
              <p className="font-semibold text-sm">{user?.nome} {user?.sobrenome}</p>
              <p className="text-xs text-petroleo-100">{user?.email}</p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-2 px-4 py-2 bg-white/10 hover:bg-white/20 rounded-lg transition"
          >
            <LogOut size={18} />
            <span>Sair</span>
          </button>
        </div>
      </aside>

      <main className="flex-1 ml-64 p-8">
        {children}
      </main>

      <VirtualAssistant />

      {isGlobalLoading && (
        <div className="fixed inset-0 z-[9999] flex items-center justify-center bg-white/40 backdrop-blur-[2px]">
          <div className="flex flex-col items-center gap-4 p-8 bg-white rounded-3xl shadow-2xl border border-gray-100 animate-in zoom-in-95 duration-200">
            <div className="w-12 h-12 border-4 border-petroleo-100 border-t-petroleo-600 rounded-full animate-spin"></div>
            <p className="text-sm font-bold text-petroleo-900 tracking-tight">Processando...</p>
          </div>
        </div>
      )}
    </div>
  );
};
