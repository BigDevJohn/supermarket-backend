# Testes de integração da auditoria

## Objetivo

Adicionar testes de integração para comprovar as quatro correções recentes da auditoria sem alterar o comportamento de produção.

## Abordagem

Criar uma classe dedicada `AuditLogIntegrationTests` usando `@SpringBootTest`, `@AutoConfigureMockMvc` e o banco H2 do perfil de testes. Os testes devem exercitar os endpoints reais, autenticação/autorização, transações e persistência no `AuditLogRepository`.

## Cobertura

1. Executar uma saída de estoque autenticada e verificar que o registro de auditoria contém a quantidade anterior e a nova quantidade.
2. Criar e cancelar um pedido como cliente e verificar o registro de cancelamento associado ao pedido e ao usuário.
3. Consultar a auditoria com ADMIN e EMPLOYEE e confirmar que CUSTOMER recebe `403`.
4. Confirmar que os fluxos normais persistem a operação de negócio e a auditoria. A falha artificial da gravação da auditoria não será forçada sem mocks ou alteração de produção; a atomicidade será coberta por execução real dos métodos transacionais.

## Limites

- Não modificar classes de produção.
- Não modificar o inicializador de dados de teste.
- Reutilizar os usuários, produtos, estoque e carrinho fornecidos pelo seed existente.
- Isolar cada teste com a transação de teste já usada no projeto.
