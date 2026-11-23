# Auditoria de alterações

## Visão geral

O módulo de auditoria registra alterações realizadas em produtos, categorias,
estoque e pedidos. Cada registro identifica a entidade afetada, a operação, o
usuário responsável, o momento da alteração e um campo `details` em JSON.

Os registros são criados durante a mesma operação de negócio. O usuário
autenticado é associado automaticamente ao registro; operações de leitura não
geram auditoria.

## Tipos de eventos

`action` informa o tipo da operação:

- `CREATION`: criação de um recurso.
- `UPDATE`: alteração de um recurso. O payload informa somente os campos que
  mudaram.
- `DELETION`: exclusão de um recurso.

`entityType` informa o recurso:

- `PRODUCT`
- `CATEGORY`
- `STOCK`
- `ORDER`

### Eventos e informações armazenadas

Eventos de criação e exclusão usam `details.attributes` para armazenar o
estado relevante:

| Evento | `details.message` | Atributos |
| --- | --- | --- |
| Criação de produto | `Product created` | `name`, `brand`, `price`, `description`, `available` |
| Exclusão de produto | `Product deleted` | `name`, `brand` |
| Criação de categoria | `Category created` | `name` |
| Exclusão de categoria | `Category deleted` | `name` |
| Criação de estoque | `Stock created` | `quantity`, `productId` |
| Criação de pedido | `Order created` | `orderId` |

Eventos de atualização usam `details.changes`. Cada item possui:

- `field`: nome do campo alterado;
- `before`: valor anterior;
- `after`: novo valor.

Os campos acompanhados são:

- Produto: `name`, `brand`, `price`, `description`, `available`.
- Categoria: `name`.
- Estoque: `quantity`.
- Pedido cancelado: `status`.

Quando nenhum campo acompanhado de produto ou categoria muda, o registro ainda
é criado com `changes: []` e uma mensagem indicando que não houve alterações.

## Exemplos de `details`

### Atualização de estoque

```json
{
  "message": "Stock updated",
  "changes": [
    {
      "field": "quantity",
      "before": 32,
      "after": 27
    }
  ]
}
```

### Cancelamento de pedido

```json
{
  "message": "Order cancelled",
  "changes": [
    {
      "field": "status",
      "before": "WAITING_PAYMENT",
      "after": "CANCELLED"
    }
  ]
}
```

## Estrutura do registro

Além de `details`, a resposta da API contém:

| Campo | Descrição |
| --- | --- |
| `id` | Identificador do registro de auditoria. |
| `action` | `CREATION`, `UPDATE` ou `DELETION`. |
| `entityType` | Tipo da entidade auditada. |
| `entityId` | Identificador da entidade; em estoque, corresponde ao produto. |
| `userId` | Usuário que realizou a operação. |
| `userName` | Nome do usuário que realizou a operação. |
| `createdAt` | Data e hora da criação do registro. |
| `details` | Payload estruturado do evento. |

## Consulta pela API

Os endpoints ficam disponíveis sob `/supermarket/audit` e exigem autenticação
com perfil `ADMIN` ou `EMPLOYEE`.

```text
GET /supermarket/audit/{id}
GET /supermarket/audit/by-entity/{entityType}/{entityId}
GET /supermarket/audit/by-entity-type/{entityType}
```

As consultas por entidade e por tipo são paginadas. É possível usar os
parâmetros padrão do Spring Data, como `page`, `size` e `sort`.

Exemplo:

```text
GET /supermarket/audit/by-entity-type/STOCK?page=0&size=20&sort=createdAt,desc
```

## Compatibilidade e persistência

`details` é persistido como JSON real no banco e exposto como objeto JSON pela
API. Valores numéricos, booleanos, textos e nulos mantêm seus tipos, sem
necessidade de interpretar strings formatadas.

Na migração de dados antigos, valores de texto não nulos são convertidos para:

```json
{
  "message": "texto original"
}
```

Valores nulos continuam nulos. A tabela possui índices para histórico por
entidade, listagem por tipo e histórico por usuário:

- `(entity_type, entity_id, created_at)`
- `(entity_type, created_at)`
- `(user_id, created_at)`

