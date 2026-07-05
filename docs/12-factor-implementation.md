# The Twelve-Factor App - Implementação no SUPER-SYS

Este documento descreve como cada um dos 12 fatores foi implementado (ou planejado) no projeto SUPER-SYS, apontando os arquivos específicos como evidência de implementação.

## 1. Codebase
- [x] **Implementação:** Todo o código-fonte está versionado de forma unificada em um único repositório Git.
  - **Evidência:** Diretório raiz do projeto: [/](file:///home/apm/Documentos/projetos/SUPER-SYS/).

## 2. Dependencies
- [x] **Implementação:** As dependências são gerenciadas de forma explícita, isolada e com todas as versões travadas diretamente nos manifestos.
  - **Evidência:** Arquivos Maven: [ai-service/pom.xml](file:///home/apm/Documentos/projetos/SUPER-SYS/ai-service/pom.xml) e [project-analysis/pom.xml](file:///home/apm/Documentos/projetos/SUPER-SYS/project-analysis/pom.xml).

## 3. Config
- [x] **Implementação:** As configurações estão estritamente isoladas do código de maneira centralizada via Spring Cloud Config Server.
  - **Evidência:** Configuração do Config Server: [config-server/src/main/resources/application.yml](file:///home/apm/Documentos/projetos/SUPER-SYS/config-server/src/main/resources/application.yml) e propriedades globais [local/application.yml](file:///home/apm/Documentos/projetos/SUPER-SYS/config-server/src/main/resources/config/local/application.yml).

## 4. Backing Services
- [x] **Implementação:** Recursos de apoio externos (PostgreSQL/pgvector, RabbitMQ, etc.) são consumidos como recursos anexados definidos por variáveis de ambiente ou configurações externas.
  - **Evidência:** Configuração de conexão do banco e RabbitMQ em [local/application.yml](file:///home/apm/Documentos/projetos/SUPER-SYS/config-server/src/main/resources/config/local/application.yml) e mapeamento do compose em [docker-compose.yml](file:///home/apm/Documentos/projetos/SUPER-SYS/docker-compose.yml).

## 5. Build, Release, Run
- [x] **Implementação:** Há separação clara dos estágios: o build empacota o código em um binário imutável e a execução (run) ocorre em contêineres Docker.
  - **Evidência:** Configurações de construção de imagem e orquestração: [Dockerfile](file:///home/apm/Documentos/projetos/SUPER-SYS/Dockerfile) e [docker-compose.yml](file:///home/apm/Documentos/projetos/SUPER-SYS/docker-compose.yml).

## 6. Processes
- [x] **Implementação:** Os microsserviços rodam como processos stateless (sem estado na memória interna). Qualquer dado persistente ou compartilhado vai para serviços de apoio.
  - **Evidência:** Controlador REST/GraphQL sem estado interno: [AiGraphQLController.java](file:///home/apm/Documentos/projetos/SUPER-SYS/ai-service/src/main/java/com/supersys/ai/controller/graphql/AiGraphQLController.java) e execução do processo via [Dockerfile](file:///home/apm/Documentos/projetos/SUPER-SYS/Dockerfile).

## 7. Port Binding
- [x] **Implementação:** A aplicação expõe seus serviços de maneira totalmente autocontida por meio de vinculação de porta de escuta HTTP/gRPC.
  - **Evidência:** Definições das portas de escuta dos microsserviços no Config Server, por exemplo: [config-server/src/main/resources/config/local/ai-service.yml](file:///home/apm/Documentos/projetos/SUPER-SYS/config-server/src/main/resources/config/local/ai-service.yml#L1-L2).

## 8. Concurrency
- [x] **Implementação:** O dimensionamento é baseado no modelo de processos, permitindo adicionar mais réplicas e instâncias de cada serviço de maneira independente.
  - **Evidência:** Orquestração de múltiplos serviços em [docker-compose.yml](file:///home/apm/Documentos/projetos/SUPER-SYS/docker-compose.yml).

## 9. Disposability
- [x] **Implementação:** Habilitado o encerramento seguro (*Graceful Shutdown*), garantindo que conexões em andamento terminem antes do processo ser finalizado.
  - **Evidência:** Ativação de desligamento gracioso centralizada: [config-server/src/main/resources/config/local/application.yml](file:///home/apm/Documentos/projetos/SUPER-SYS/config-server/src/main/resources/config/local/application.yml#L1-L7).

## 10. Dev/Prod Parity
- [x] **Implementação:** Mantém-se paridade de ambientes usando perfis equivalentes e subindo os mesmos serviços via Docker Compose tanto no ambiente de desenvolvimento local quanto nos servidores de teste.
  - **Evidência:** Perfis de configuração estruturados: diretórios [config/local](file:///home/apm/Documentos/projetos/SUPER-SYS/config-server/src/main/resources/config/local) e [config/dev](file:///home/apm/Documentos/projetos/SUPER-SYS/config-server/src/main/resources/config/dev).

## 11. Logs
- [x] **Implementação:** Logs são tratados como fluxo contínuo de eventos jogados na saída padrão (stdout) e gerenciados pelo console ou coletores de containers.
  - **Evidência:** Logs direcionados para saída padrão e rastreabilidade nos consoles gerenciados pelo [docker-compose.yml](file:///home/apm/Documentos/projetos/SUPER-SYS/docker-compose.yml).

## 12. Admin Processes
- [x] **Implementação:** Tarefas administrativas e de banco de dados rodam como scripts/processos avulsos integrados à mesma base de código (ex: migrações Flyway).
  - **Evidência:** Scripts Flyway de alteração e carga de dados em lote: [V1__init_schema_and_seed.sql](file:///home/apm/Documentos/projetos/SUPER-SYS/project-analysis/src/main/resources/db/migration/V1__init_schema_and_seed.sql).
