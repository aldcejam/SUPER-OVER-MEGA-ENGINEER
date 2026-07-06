# Checklist de Recursos Implementados - SUPER-SYS

Este documento compila e organiza as tecnologias, serviços e funcionalidades atualmente implementados no ecossistema SUPER-SYS.

## Checklist de Implementação

### 1. Observabilidade & Monitoramento
- [x] **Grafana (Porta 3000)**: Visualização centralizada de métricas e dashboards (JVM, Resilience4j, etc.).
- [x] **Grafana Tempo**: Rastreamento distribuído (*distributed tracing*) de requisições entre microsserviços.
- [x] **Grafana Loki**: Agregação e visualização de logs centralizados.
- [x] **Prometheus (Porta 9090)**: Coleta e armazenamento de métricas expostas via endpoints do Actuator.
- [x] **Rastro de Logs**: Correlação de logs ponta a ponta nas transações.
- [x] **Traces Distribuídos (Trace)**: Rastreabilidade ponta a ponta configurada via OpenTelemetry Collector.

### 2. Serviços & Microsserviços
- [x] **`project-analysis`**: Microsserviço de negócio que gerencia o ciclo de vida (CRUD) de Projetos, Cronogramas e Alocações de Recursos.
- [x] **`ai-service`**: Microsserviço de Inteligência Artificial integrado ao Spring AI e DeepSeek para análises de cronogramas e projetos.
- [x] **`lambda-service`**: Microsserviço Java que simula o processamento Serverless (AWS Lambda) para extração local de dados de arquivos PDF.
- [x] **`api-gateway`**: Gateway unificado na porta 8080 para roteamento e balanceamento de carga.
- [x] **`eureka-server`**: Cluster de Service Discovery para registro dinâmico dos microsserviços.
- [x] **`config-server`**: Configuração centralizada alimentada por repositório Git local.

### 3. Armazenamento de Dados & Vetores
- [x] **PostgreSQL (Banco Relacional)**: Armazenamento e persistência dos dados relacionais do negócio.
- [x] **PostgreSQL com `pgvector` (Banco Vetorial)**: Banco vetorial para a base de conhecimento da Inteligência Artificial.
- [x] **Pipeline de IA com PDFs (RAG)**: Inserção de dados vetoriais gerados a partir do texto extraído de arquivos PDF enviados para permitir buscas semânticas contextuais.

### 4. Inteligência Artificial
- [x] **Provedor de IA (DeepSeek)**: Utilizado como o modelo de linguagem (LLM) principal no `ai-service` para realizar as análises.
- [x] **Integração com GitHub MCP (Model Context Protocol)**: Habilita consultas dinâmicas de desenvolvimento diretamente no repositório através de IA.

---

## Documentação Relacionada
* [The Twelve-Factor App - Status de Implementação](docs/12-factor-implementation.md)