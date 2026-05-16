export const initTestData = () => {
  // Verificar se já existem usuários
  const existingUsers = localStorage.getItem('marketpay_users');

  if (!existingUsers) {
    // Criar usuários de teste
    const testUsers = [
      {
        id: 'user_cliente',
        nome: 'João',
        sobrenome: 'Silva',
        cpf: '123.456.789-00',
        endereco: 'Rua das Flores, 123, São Paulo - SP',
        email: 'cliente@test.com',
        senha: '123456',
        role: 'cliente',
        createdAt: new Date().toISOString(),
      },
      {
        id: 'user_loja',
        nome: 'Maria',
        sobrenome: 'Santos',
        cpf: '987.654.321-00',
        endereco: 'Av. Principal, 456, São Paulo - SP',
        email: 'loja@test.com',
        senha: '123456',
        role: 'dono_loja',
        createdAt: new Date().toISOString(),
      },
    ];

    localStorage.setItem('marketpay_users', JSON.stringify(testUsers));

    // Criar contas correntes para os usuários
    const testContas = [
      {
        id: 'conta_cliente',
        numero: '123456-78',
        saldo: 5000,
        dataCriacao: new Date().toISOString(),
        clienteId: 'user_cliente',
      },
      {
        id: 'conta_loja',
        numero: '987654-32',
        saldo: 10000,
        dataCriacao: new Date().toISOString(),
        clienteId: 'user_loja',
      },
    ];

    localStorage.setItem('marketpay_contas', JSON.stringify(testContas));
  }
};
