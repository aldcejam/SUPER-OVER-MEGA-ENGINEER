---
name: Integration and E2E Testing
description: Guideline and template for creating Integration and E2E tests in the SUPER-SYS architecture using Testcontainers, REST Assured, and JUnit 5.
---

# Estratégia de Testes: Integração e E2E no SUPER-SYS

Esta skill define o padrão arquitetural para a construção de testes no projeto SUPER-SYS. Sempre que solicitado para criar testes para rotas (REST ou GraphQL), siga as regras abaixo.

## 1. Stack Tecnológico
- **Integração com Banco de Dados:** `Testcontainers` (PostgreSQL/pgvector).
- **Requisições API:** `REST Assured` para chamadas HTTP e GraphQL.
- **Framework Base:** `JUnit 5` e `SpringBootTest` com porta aleatória (`RANDOM_PORT`).

## 2. Testes de Integração (Padrão)
Os testes de integração devem focar em testar a aplicação com dependências de banco de dados reais, mas **sem fazer chamadas externas reais** que gerem custos ou latência excessiva.

- **Classe Base:** Utilize ou estenda `AbstractIntegrationTest` (que desativa Eureka/Config Server e provê a injenção do `PostgreSQLContainer`).
- **Comportamento Externo:** Serviços como RabbitMQ, APIs da OpenAI/DeepSeek e AWS S3 devem ser *mockados* (usando `Mockito` ou `WireMock`) durante os testes de integração.
- **Gatilho de Execução:** Rodam a cada commit (build padrão do Maven via `mvn clean test`).

## 3. Testes End-to-End (E2E)
Os testes E2E validam o fluxo completo, desde a entrada do usuário até o acionamento de serviços reais externos na nuvem.

- **Nomenclatura:** Classes devem terminar com `E2ETest` (ex: `ProjectE2ETest.java`).
- **Comportamento Externo:** Bate **diretamente nas APIs externas** (OpenAI, S3, RabbitMQ). Nenhuma chamada externa é mockada.
- **Gatilho de Execução:** Executados exclusivamente sob demanda através de um profile do Maven (`mvn clean test -P e2e`).

## 4. Exemplo de Implementação de Teste (GraphQL)
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExampleIntegrationTest extends AbstractIntegrationTest {
    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void shouldExecuteGraphQLQuery() {
        String graphqlQuery = "{ \"query\": \"{ findAllProjects { id name } }\" }";
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(graphqlQuery)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("data.findAllProjects", notNullValue());
    }
}
```

## Diretrizes de Uso para o Agente IA
1. Ao receber a tarefa de criar um teste, verifique qual é a rota e o domínio do microsserviço.
2. Identifique quais dependências externas a rota utiliza.
3. Decida, baseado nas regras acima, se o caso se aplica a um Teste de Integração ou Teste E2E (ou ambos).
4. Siga a padronização e o perfil Maven (se for E2E).
