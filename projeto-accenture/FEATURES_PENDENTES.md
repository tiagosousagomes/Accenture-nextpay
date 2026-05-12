# Funcionalidades pendentes no nextPay (descobertas durante a integração)

Este documento lista o que o frontend já espera fazer, mas que ainda não tem
suporte no backend `nextPay`. Cada item descreve o uso no front, o endpoint
sugerido e por que ele é necessário. Nenhum desses pontos foi implementado
nesta integração — eles ficam como backlog.

## 1. Autenticação (`/api/auth/*`)

**Uso no front:** `LoginPage`, `AuthContext.login`.

Hoje não há endpoint de autenticação no nextPay. O `AuthContext.register` chama
`POST /api/clientes` corretamente, mas o login foi feito como um *lookup
client-side* contra um cache em `localStorage` (`nextpay_credenciais`) que guarda
`{email, senha, clienteId}` no momento do cadastro. É **suficiente para o
MVP**, mas não autentica ninguém de fato — qualquer pessoa com acesso ao
navegador vê as credenciais.

**Endpoints sugeridos:**

- `POST /api/auth/login` → recebe `{email, senha}`, valida senha (BCrypt) e
  devolve algo como `{token, cliente: ClienteResponse}`.
- `POST /api/auth/logout`.
- `POST /api/auth/recuperar-senha` → fluxo de email com token de reset.

## 2. Listagem de pedidos por comprador / vendedor

**Uso no front:** `PedidosPage` (cliente), `PedidosLojaPage` e
`RelatoriosPage` (vendedor).

O nextPay só tem `GET /api/pedidos/{id}`. Para listar os pedidos do usuário
logado, o front mantém um cache local de IDs em `localStorage`
(`nextpay_pedidos_locais`) e busca cada pedido individualmente. Isso quebra
quando o usuário troca de dispositivo ou limpa o cache.

**Endpoints sugeridos:**

- `GET /api/clientes/{compradorId}/pedidos` — lista pedidos onde o cliente é
  comprador.
- `GET /api/clientes/{vendedorId}/pedidos-recebidos` — lista pedidos que
  contém produtos desse vendedor.
- Opcional: filtro por `?status=CRIADO|PAGO|ENVIADO|ENTREGUE|CANCELADO`.

Sem isso, as telas `PedidosLojaPage` e `RelatoriosPage` ficam apenas com um
placeholder informando que a feature está pendente.

## 3. Saque e transferência genérica

**Uso no front:** botões "Sacar" e "Transferir" em `ContaCorrentePage`.

`POST /api/transacoes` aceita só `DEPOSITO` e `PIX`. Não há como sacar
(reduzir saldo sem pagar pedido) nem transferir entre contas próprias do
mesmo cliente fora do fluxo PIX.

**Endpoints sugeridos:**

- `POST /api/transacoes` aceitando `tipo: SAQUE` (debita saldo, registra
  transação).
- Ou um endpoint dedicado `POST /api/contas/{id}/saque`.
- Para transferências internas entre contas do mesmo titular, considerar
  reutilizar PIX ou criar `POST /api/contas/{origem}/transferir-para/{destino}`.

Hoje os botões mostram apenas um toast informativo.

## 4. Cancelar / confirmar pedido

**Uso no front:** o fluxo antigo tinha "Reservar → Confirmar → Pagar".

No nextPay o ciclo é simples: `POST /api/pedidos` (status `CRIADO` e estoque
já é reservado) → `POST /api/pedidos/{id}/pagar` (status `PAGO`).

**O que falta:**

- `PUT /api/pedidos/{id}/cancelar` para liberar estoque de pedidos `CRIADO`
  que o cliente desistiu antes de pagar.
- Transições subsequentes do ciclo (`ENVIADO`, `ENTREGUE`) — hoje só `CRIADO`
  e `PAGO` viram de fato. Os enums já existem em `Pedido.StatusPedido`, mas
  não há endpoint para avançar.

## 5. Atualização de dados do cliente

**Uso no front:** `PerfilPage` (modo edição).

Não existe `PUT /api/clientes/{id}`. Hoje a página exibe o perfil em modo
somente leitura.

**Endpoint sugerido:** `PUT /api/clientes/{id}` recebendo um subset de
`ClienteRequest` (nome, email, tipo). CPF idealmente não muda.

## 6. Loja aberta / fechada (toggle do vendedor)

**Uso no front:** botão "Loja Aberta / Loja Fechada" em `ProdutosPage`.

O nextPay não modela "loja". Vendedor é só um `Cliente` com `tipo=VENDEDOR`
ou `AMBOS`. Não há um flag global para pausar vendas.

**Sugestão:** adicionar `boolean lojaAberta` em `Cliente` (ou em uma entidade
`Loja` separada). Quando `false`, `POST /api/pedidos` rejeita itens desse
vendedor.

Por enquanto o front simplesmente removeu esse toggle.

## 7. Recuperação de senha

**Uso no front:** `RecuperarSenhaPage`. Hoje exibe um toast avisando que a
feature está indisponível. Veja item 1.

## 8. Pesquisa / listagem de cliente por email ou CPF

**Uso no front:** o login precisa achar um cliente pelo email. Hoje só
funciona porque guardamos `email → clienteId` no localStorage no momento do
cadastro.

**Endpoint sugerido:** `GET /api/clientes?email=X` ou similar (idealmente só
servidor, depois do `/api/auth/login`).

## 9. Catálogo: filtros e paginação

**Uso no front:** `MarketplacePage` lista todos os produtos.

O `GET /api/produtos` aceita `?nome=`, mas faltam:

- Paginação (`?page=&size=`).
- Filtro por categoria.
- Filtro de "apenas em estoque" / "apenas ativos".

Não é bloqueante hoje (catálogo é pequeno), mas vai escalar mal.

## 10. Status `FAILED` em pedido

**Uso no front:** a tela antiga mostrava `FAILED`. O backend não modela esse
estado — pagamento que falha lança `BusinessException` mas o pedido continua
`CRIADO`. O front foi ajustado para refletir só os estados que o backend
suporta.

---

## Notas gerais

- Todos os IDs no nextPay são `UUID`. O front trata como `string`.
- `ApiError` (timestamp, status, error, message, path, errors) é o formato
  padrão de erro do backend. O front exibe `message` + lista de campos
  inválidos via `NextPayApiError` em `src/app/services/api.ts`.
- O `BASE_URL` é configurável via `VITE_API_URL`. Padrão: `http://localhost:8080`.
- Autenticação ainda não tem token — o `X-User-Id` antigo foi removido e os
  endpoints recebem UUIDs explícitos no body/path conforme exigência do
  controller.
