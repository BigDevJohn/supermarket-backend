 # Supermarket Application

Este projeto é uma API REST para gerenciamento de um supermercado, desenvolvida com **Java** e **Spring Boot**. A aplicação possui recursos como gerenciamento de produtos, categorias, carrinhos, pedidos e upload/download de imagens. Além disso, conta com autenticação e autorização via **JWT**, documentação com **Swagger** e suporte para dois perfis de banco de dados: **H2** (banco em memória) e **MySQL**.

## Tecnologias Utilizadas
- **Java 17**
- **Spring Boot 3.3.6**
- **H2 Database**
- **MySQL**
- **Spring Security**
- **JWT (JSON Web Token)**
- **SpringDoc OpenAPI (Swagger)**

## Configuração dos Bancos de Dados

A aplicação suporta dois perfis de banco de dados:
- **test**: Usa o H2 (banco em memória) para desenvolvimento ou testes.
- **dev**: Usa o MySQL para armazenamento persistente.

### Configurando os Perfis

1. **H2 (test)**  
   Configuração no arquivo `application-test.properties`:
   ```properties
   spring.datasource.url=jdbc:h2:mem:testdb
   spring.datasource.username=jv
   spring.datasource.password=
   spring.h2.console.enabled=true
   spring.h2.console.path=/h2-console
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true
O console H2 pode ser acessado em: http://localhost:8080/h2-console

2. **MySQL (dev)**
    Configuração no arquivo `application-dev.properties`:
    
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/supermarket
    spring.datasource.username=[seuUsername]
    spring.datasource.password=[suaSenha]
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.format_sql=true
    ```

3. **Ativando um Perfil**
O perfil ativo é definido no arquivo `application.properties`:
    
    ```properties
    spring.profiles.active=test
    ```
    Altere para dev se quiser usar o MySQL.

### Documentação da API com Swagger
A documentação dos endpoints pode ser acessada em:
http://localhost:8080/swagger-ui.html

Nota:No arquivo WebSecurityConfig, as URLs do Swagger já estão liberadas:

```java
http.authorizeHttpRequests(authorize -> authorize
    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
        .permitAll());
```
### Configuração de Upload de Arquivos
A aplicação suporta upload de arquivos com tamanho máximo configurado em `application.properties`:

```properties
spring.servlet.multipart.max-file-size = 8MB
```
Tamanho máximo: 8 MB

### Executando a Aplicação

Inicie a aplicação:

```bash
./mvnw spring-boot:run
```
Acesse o Swagger para explorar os endpoints: http://localhost:8080/swagger-ui.html

### Configuração de Segurança
A autenticação e autorização são implementadas com JWT. O segredo do token está definido no arquivo `application.properties`.
O admin padrão usa admin@gmail.com como login e 123456 como senha.


### Contato
Se você tiver dúvidas, sugestões ou encontrar algum problema, sinta-se à vontade para abrir uma issue.
