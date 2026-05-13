export type UserRole = 'user';

export interface Cliente {
  id: string;
  nome: string;
  sobrenome: string;
  cpf: string;
  endereco: string;
  cep?: string;
  cidade?: string;
  estado?: string;
  bairro?: string;
  email: string;
  role: UserRole;
  createdAt: string;
  fotoPerfil?: string;
  nomeLoja?: string;
  descricaoLoja?: string;
}

export interface ContaCorrente {
  id: string;
  numero: string;
  saldo: number;
  dataCriacao: string;
  clienteId: string;
}

export interface Transacao {
  id: string;
  tipo: 'deposito' | 'saque' | 'transferencia_enviada' | 'transferencia_recebida' | 'pix_enviado' | 'pix_recebido' | 'pagamento_pedido' | 'transferencia';
  valor: number;
  data: string;
  descricao: string;
  contaId: string;
  contaDestinoId?: string;
  status: 'sucesso' | 'falha';
}

export interface Produto {
  id: string;
  sku: string;
  nome: string;
  categoria: string;
  preco: number;
  estoque: number;
  imagem?: string;
  descricao?: string;
  vendedorId: string; // ID do dono da loja
  vendedorNome?: string; // Nome da loja
  vendedorFoto?: string; // Foto da loja
}

export type StatusPedido = 'RESERVED' | 'PAID' | 'FAILED' | 'CANCELED' | 'EXPIRED';

export interface ItemPedido {
  produtoId: string;
  quantidade: number;
  precoUnitario: number;
}

export interface Pedido {
  id: string;
  clienteId: string;
  vendedorId: string;
  vendedorNome?: string;
  produtoNome?: string;
  itens: ItemPedido[];
  total: number;
  status: StatusPedido;
  dataCriacao: string;
  dataPagamento?: string;
}

export interface CarrinhoItem {
  idPedido?: string;
  produto: Produto;
  quantidade: number;
}
