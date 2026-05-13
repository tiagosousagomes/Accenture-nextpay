import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { BankProvider } from './contexts/BankContext';
import { MarketplaceProvider } from './contexts/MarketplaceContext';
import { RewardsProvider } from './contexts/RewardsContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Toaster } from 'sonner';

import { LandingPage } from './pages/LandingPage';
import { LoginPage } from './pages/LoginPage';
import { CadastroPage } from './pages/CadastroPage';
import { RecuperarSenhaPage } from './pages/RecuperarSenhaPage';
import { DashboardPage } from './pages/DashboardPage';
import { ContaCorrentePage } from './pages/ContaCorrentePage';
import { ExtratoPage } from './pages/ExtratoPage';
import { MarketplacePage } from './pages/MarketplacePage';
import { PedidosPage } from './pages/PedidosPage';
import { RecompensasPage } from './pages/RecompensasPage';
import { ProdutosPage } from './pages/ProdutosPage';
import { PedidosLojaPage } from './pages/PedidosLojaPage';
import { RelatoriosPage } from './pages/RelatoriosPage';
import { PerfilPage } from './pages/PerfilPage';

import { initTestData } from './utils/initData';
import { useEffect } from 'react';

export default function App() {
  useEffect(() => {
    initTestData();
  }, []);

  return (
    <BrowserRouter>
      <AuthProvider>
        <BankProvider>
          <MarketplaceProvider>
            <RewardsProvider>
              <Toaster position="top-right" richColors />
              <Routes>
                <Route path="/" element={<LandingPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/cadastro" element={<CadastroPage />} />
                <Route path="/recuperar-senha" element={<RecuperarSenhaPage />} />

              <Route
                path="/dashboard"
                element={
                  <ProtectedRoute>
                    <DashboardPage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/conta"
                element={
                  <ProtectedRoute>
                    <ContaCorrentePage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/extrato"
                element={
                  <ProtectedRoute>
                    <ExtratoPage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/marketplace"
                element={
                  <ProtectedRoute>
                    <MarketplacePage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/pedidos"
                element={
                  <ProtectedRoute>
                    <PedidosPage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/recompensas"
                element={
                  <ProtectedRoute>
                    <RecompensasPage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/produtos"
                element={
                  <ProtectedRoute>
                    <ProdutosPage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/pedidos-loja"
                element={
                  <ProtectedRoute>
                    <PedidosLojaPage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/relatorios"
                element={
                  <ProtectedRoute>
                    <RelatoriosPage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/perfil"
                element={
                  <ProtectedRoute>
                    <PerfilPage />
                  </ProtectedRoute>
                }
              />

                <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            </RewardsProvider>
          </MarketplaceProvider>
        </BankProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}