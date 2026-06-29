## Acesso ao Eureka interface
```
http://localhost:8761
```

## Arquitetura e Responsabilidades dos Serviços

* **api-gateway (Porta 8080)**: Ponto de entrada unificado da arquitetura. Responsável pelo roteamento de requisições, filtros de requisição/resposta (StripPrefix) e balanceamento de carga entre serviços.
* **eureka-server (Portas 8761-8763)**: Cluster de Service Discovery. Gerencia o registro dinâmico e localização de todas as instâncias de microsserviços ativas.
* **config-server (Porta 8888)**: Servidor de configuração centralizado conectado a um repositório Git local. Serve propriedades de ambiente em tempo de execução para os microsserviços.
* **project-analysis (Porta 8083)**: Microsserviço de negócio. Gerencia Projetos, Cronogramas e Alocações de Recursos, persistindo dados relacionais e disparando requisições assíncronas de análise.
* **ai-service (Porta 8082)**: Microsserviço de inteligência artificial integrado com Spring AI. Expõe endpoints REST e GraphQL para gerar análises estruturadas em JSON de Cronogramas, Viabilidade de Projetos e Alocação de Recursos (gargalos e custos).

## Mapeamento de Métodos e Endpoints

### 1. project-analysis (GraphQL API - Porta 8083)

#### Queries (Consultas)
* **`findAllProjects`**: Retorna a lista de todos os projetos cadastrados.
* **`findProjectById(id)`**: Busca um projeto específico pelo seu ID (incluindo cronogramas e alocações).
* **`findAllSchedules`**: Retorna a lista de todos os cronogramas cadastrados.
* **`findScheduleById(id)`**: Busca um cronograma específico pelo seu ID.

#### Mutations (Operações de Escrita)
* **`createProject(input)`**: Cadastra um novo projeto com seu orçamento e alocações iniciais.
* **`updateProject(id, input)`**: Atualiza dados, orçamento ou equipe de um projeto.
* **`deleteProject(id)`**: Remove um projeto do sistema.
* **`requestProjectAnalysis(id)`**: Dispara de forma assíncrona a análise de viabilidade do projeto via chamada REST para o `ai-service`.
* **`requestResourceAnalysis(id)`**: Dispara de forma assíncrona a análise de gargalos de equipe via chamada REST para o `ai-service`.
* **`createSchedule(input)`**: Cadastra um novo cronograma com suas etapas e durações.
* **`updateSchedule(id, input)`**: Atualiza etapas ou prazos de um cronograma.
* **`deleteSchedule(id)`**: Remove um cronograma.
* **`requestAnalysis(id)`**: Dispara de forma assíncrona a análise de viabilidade do cronograma via chamada REST para o `ai-service`.

---

### 2. ai-service (REST & GraphQL API - Porta 8082)

#### Endpoints REST (POST)
* **`POST /api/ai/analyze-schedule`**: Recebe o payload do cronograma e retorna um JSON padronizado (`ScheduleAnalysisResponse`) com status, estimativa de dias e riscos.
* **`POST /api/ai/analyze-project`**: Recebe o payload do projeto (com cronograma associado) e retorna um JSON padronizado (`ProjectAnalysisResponse`) com nota de viabilidade e riscos financeiros.
* **`POST /api/ai/analyze-resources`**: Recebe o payload do projeto (com suas alocações de recursos) e retorna um JSON padronizado (`ResourceAnalysisResponse`) identificando sobrecargas de equipe e gargalos.

#### API GraphQL (Queries & Mutations)
* **`Query: askProjectQuestion(prompt)`**: Assistente de desenvolvimento que utiliza ferramentas do GitHub (via MCP) para buscar informações no repositório `aldcejam/SUPER-OVER-MEGA-ENGINEER`.
* **`Query: askDeepSeek(prompt)`**: Assistente enriquecido com busca semântica (RAG) utilizando a base de vetores `pgvector`.
* **`Mutation: analyzeSchedule(schedule)`**: Realiza a análise de cronograma (mesma funcionalidade da rota REST `/api/ai/analyze-schedule`).

---

### 3. Observabilidade e Portas de Infraestrutura
* **Grafana (Porta 3000)**: Visualização de Dashboards (Dashboard global JVM & Resilience4j Circuit Breaker).
* **Prometheus (Porta 9090)**: Coleta e armazenamento de métricas.
* **Métricas do Actuator**: Disponíveis em `/actuator/prometheus` em todos os microsserviços (ex: `http://localhost:8082/actuator/prometheus`).