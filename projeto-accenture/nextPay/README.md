# SeuBanco — MVP Banking + Marketplace + Cashback

MVP de Banco Digital com Marketplace Interno e Programa de Fidelidade (Cashback + Gamificação).

## Stack

- Java 21
- Spring Boot 4.0.6
- Spring Data JPA / Hibernate
- Bean Validation
- H2 (in-memory)
- Lombok
- Maven

## Como rodar

### Pré-requisitos
- JDK 21
- Maven 3.9+

### Executar
```bash
./mvnw spring-boot:run
# ou
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

### Console H2
Acesse `http://localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:mem:seubanco`
- User: `sa`
- Password: *(em branco)*

## Estrutura de pacotes

```
com.seubanco
├── SeuBancoApplication.java
├── config/         → DataSeeder (regras de cashback iniciais)
├── entity/         → 11 entidades JPA
├── repository/     → JpaRepositories
├── dto/            → Records Request/Response
├── service/        → ClienteService, PedidoService, TransacaoService, CashbackService
├── controller/     → 4 REST Controllers
└── exception/      → BusinessException, ResourceNotFoundException, GlobalExceptionHandler
```

## Endpoints principais

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/clientes` | Cria cliente + endereço + conta ATIVA |
| GET | `/api/clientes/{id}` | Busca cliente |
| GET | `/api/clientes/{id}/extrato` | Extrato de transações |
| POST | `/api/pedidos` | Cria pedido (snapshot de preço, reserva estoque) |
| GET | `/api/pedidos/{id}` | Busca pedido |
| POST | `/api/transacoes` | Registra transação (calcula cashback automático) |
| GET | `/api/cashback/saldo/{clienteId}` | Saldo de pontos + nível |
| POST | `/api/cashback/resgatar` | Resgata pontos (100 pts = R$ 1,00) |
| POST | `/api/cashback/liberar-pendentes` | Libera cashbacks pendentes (>30 dias) |

## Regras de Cashback

- Cálculo: `valor × (percentual / 100)`
- Match por `categoria + nível do cliente`
- 100 pontos = R$ 1,00
- Nasce PENDENTE → vira saldo após 30 dias
- Liberação instantânea para nível OURO ou PLATINA
- Níveis: 0–999 BRONZE • 1000–2999 PRATA • 3000–6999 OURO • 7000+ PLATINA

## Exemplo de payload

### Criar cliente
```json
POST /api/clientes
{
  "nome": "Maria Silva",
  "cpf": "12345678901",
  "email": "maria@email.com",
  "senha": "senha123",
  "tipo": "AMBOS",
  "enderecos": [
    {
      "cep": "58400-000",
      "logradouro": "Rua das Flores",
      "numero": "123",
      "complemento": "Apto 4",
      "cidade": "Campina Grande",
      "uf": "PB",
      "tipo": "RESIDENCIAL"
    }
  ]
}
```

### Registrar transação
```json
POST /api/transacoes
{
  "contaId": "uuid-da-conta",
  "tipo": "COMPRA",
  "categoria": "MARKETPLACE",
  "valor": 150.00,
  "descricao": "Compra de teste"
}
```

### Resgatar cashback
```json
POST /api/cashback/resgatar
{
  "clienteId": "uuid-do-cliente",
  "pontos": 500
}
```

## Abrir no VSCode

1. Instale o **Extension Pack for Java** e o **Spring Boot Extension Pack** (sugeridos em `.vscode/extensions.json`).
2. Abra a pasta do projeto.
3. Aguarde a indexação do Java/Maven.
4. Rode pelo `SeuBancoApplication.java` (botão **Run**) ou pelo terminal: `mvn spring-boot:run`.

## Notas de produção

- Senha está com hash simples para o MVP. Trocar por **BCrypt** + Spring Security antes de produção.
- DDL `update` para dev/MVP. Migrar para **Flyway** ou **Liquibase** em produção.
- H2 in-memory. Trocar por **PostgreSQL** em produção.
- Adicionar autenticação (JWT/OAuth2), rate limiting, observabilidade (Actuator + Micrometer) e testes de integração.
