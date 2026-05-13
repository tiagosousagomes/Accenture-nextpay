# Academia JAVA Accenture - Frontend Project 🚀

Este é o frontend da aplicação desenvolvida para a **Academia JAVA da Accenture**. O projeto integra funcionalidades de um banco digital moderno com um marketplace completo, incluindo um sistema de recompensas.

## 🛠️ Tecnologias Utilizadas

O projeto foi construído utilizando as tecnologias mais modernas do ecossistema Web:

- **[React](https://reactjs.org/)** (v18) - Biblioteca principal para construção da interface.
- **[TypeScript](https://www.typescriptlang.org/)** - Adiciona tipagem estática ao JavaScript para maior segurança e produtividade.
- **[Vite](https://vitejs.dev/)** - Tooling ultrarrápido para desenvolvimento e build.
- **[Tailwind CSS](https://tailwindcss.com/)** (v4) - Framework CSS utility-first para estilização moderna e responsiva.
- **[Radix UI / Shadcn](https://www.radix-ui.com/)** - Componentes de interface acessíveis e altamente customizáveis.
- **[Framer Motion](https://www.framer.com/motion/)** - Biblioteca para animações fluidas e micro-interações.
- **[React Router](https://reactrouter.com/)** (v7) - Gerenciamento de rotas e navegação.
- **[Recharts](https://recharts.org/)** - Visualização de dados e gráficos financeiros.
- **[Lucide React](https://lucide.dev/)** - Conjunto de ícones minimalistas e modernos.
- **[jsPDF](https://rawgit.com/MrRio/jsPDF/master/docs/index.html)** - Geração de documentos PDF (extratos bancários).

## ✨ Funcionalidades Principais

### 🏦 Sistema Bancário
- **Dashboard Financeiro:** Visualização de saldo, métricas de gastos e ganhos em tempo real.
- **Histórico de Transações:** Lista completa e detalhada de todas as movimentações financeiras.
- **Exportação de Extrato:** Funcionalidade para gerar PDF do extrato bancário.
- **Gestão de Saldo:** Integração com backend para operações de depósito e transferências.

### 🛒 Marketplace
- **Catálogo de Produtos:** Listagem de produtos disponíveis para compra com MarketCoins.
- **Carrinho de Compras:** Sistema de reserva e confirmação de pedidos em dois passos.
- **Histórico de Pedidos:** Acompanhamento de compras realizadas e status de entrega.
- **Gestão de Loja:** Funcionalidade para usuários gerenciarem seus próprios produtos à venda.

### 🏆 Gamificação & Recompensas
- **MarketCoins:** Moeda virtual do ecossistema ganha através de atividades.
- **Sistema de Níveis:** Progressão de perfil baseada em conquistas e transações.
- **Recompensas:** Troca de pontos por benefícios exclusivos dentro da plataforma.

## 📂 Estrutura do Projeto

```text
src/
└── app/
    ├── components/    # Componentes reutilizáveis (UI, Layout, etc)
    ├── contexts/      # Contextos do React (Auth, Bank, Cart)
    ├── pages/         # Páginas principais da aplicação
    ├── services/      # Integração com APIs e serviços externos
    ├── types/         # Definições de tipos TypeScript
    ├── utils/         # Funções utilitárias e formatadores
    └── App.tsx        # Configuração de rotas e providers
```

## 🚀 Como Executar o Projeto

### Pré-requisitos
- Node.js (versão 18 ou superior)
- Gerenciador de pacotes (npm, yarn ou pnpm)

### Instalação
1. Clone o repositório.
2. Navegue até a pasta do frontend:
   ```bash
   cd frontend
   ```
3. Instale as dependências:
   ```bash
   npm install
   ```

### Execução
Para iniciar o servidor de desenvolvimento:
```bash
npm run dev
```
Acesse `http://localhost:5173` no seu navegador.

---
Desenvolvido pela equipe 9:
