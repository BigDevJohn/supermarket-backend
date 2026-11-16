# Details estruturado e índices de auditoria

## Objetivo

Tornar o histórico de auditoria consultável de forma confiável e preparar a tabela para crescimento, sem criar novas entidades de negócio.

## Details estruturado

O campo `audit_logs.details` será migrado de texto para JSON e representado por `JsonNode` em `AuditLog` e `AuditLogDTO`. O contrato HTTP passará a devolver `details` como objeto JSON.

Os payloads usarão uma forma consistente:

- Criação/exclusão: `message` e `attributes`.
- Atualização: `message` e `changes`, onde cada alteração tem `field`, `before` e `after`.
- Cancelamento de pedido: alteração de `status`, incluindo o status anterior e `CANCELLED`.

O `AuditLogService` construirá os nós JSON com Jackson, evitando concatenação manual e preservando tipos numéricos, booleanos e nulos. O cancelamento receberá o status anterior para que o histórico não dependa apenas do estado posterior do pedido.

## Migração

Criar uma migração Flyway V4 que:

1. adicione uma coluna JSON temporária;
2. converta cada valor textual existente para um objeto com a chave `message` contendo o texto original;
3. substitua a coluna antiga pela coluna JSON `details`;
4. crie os índices `(entity_type, entity_id, created_at)`, `(entity_type, created_at)` e `(user_id, created_at)`.

Valores nulos devem continuar nulos. A migração precisa funcionar no MySQL usado pelo perfil `dev`; o perfil `test` usa o schema gerado pelo Hibernate e deve continuar inicializando com H2.

## Índices

- `(entity_type, entity_id, created_at)` atende ao histórico de uma entidade em ordem temporal.
- `(entity_type, created_at)` atende à listagem por tipo.
- `(user_id, created_at)` prepara consultas de auditoria por usuário e suporta a relação existente.

## Testes

Atualizar os testes de integração para validar `details` como JSON, incluindo mudanças de estoque, cancelamento e listagem autorizada. Verificar também que o perfil `test` continua inicializando e que a suíte completa passa. A conversão de dados antigos será validada por revisão da migração e por um teste de persistência compatível com o banco de testes, sem depender de MySQL externo.

## Limites

- Não auditar leituras, login ou pagamentos inexistentes.
- Não criar tabela filha de alterações.
- Não remover filtros ou paginação existentes.
