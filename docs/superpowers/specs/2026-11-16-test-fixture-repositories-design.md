# Seed de testes sem auditoria

## Objetivo

Permitir que o estado inicial usado pelos testes seja criado sem depender de um
usuário autenticado e sem gerar registros artificiais em `audit_logs`.

## Escopo

Alterar o `TestDatabaseInitializer` para persistir diretamente as fixtures por
meio dos repositórios necessários. Os dados, valores e a ordem de criação devem
permanecer compatíveis com os testes existentes, incluindo os IDs esperados.

## Fluxo

1. Manter a criação dos usuários pelo `UserService`, pois ela também cria os
   carrinhos e atribui os papéis.
2. Criar e salvar categorias pelo `CategoryRepository`.
3. Criar produtos, associar as categorias e salvar pelo `ProductRepository`.
4. Criar e salvar estoques pelo `StockRepository`, associados aos produtos.
5. Manter a preparação do carrinho do cliente, mas persistir seus itens sem
   passar por serviços que alteram estoque ou geram auditoria.

O seeder não deve chamar `CategoryService`, `ProductService`, `StockService` ou
`CartItemService`. As operações reais executadas nos testes continuam usando os
serviços e permanecem sujeitas à autenticação e à auditoria.

## Validação

- O contexto Spring deve iniciar com o perfil `test`.
- Os testes existentes devem continuar encontrando os mesmos produtos, estoque,
  categorias e carrinho.
- O bootstrap não deve falhar por ausência de usuário no `SecurityContext`.
- A inicialização não deve criar logs de auditoria.
