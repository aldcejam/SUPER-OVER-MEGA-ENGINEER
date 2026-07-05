# The Twelve-Factor App - Checklist e Plano de Ação

Este documento contém um checklist e um plano de ação para verificar a aderência do projeto **SUPER-SYS** aos princípios do [The Twelve-Factor App](https://12factor.net/pt_br/).

## 1. Codebase (Base de Código)
*Uma base de código com rastreamento de versão, muitos deploys.*

- [x] **Verificação:** O projeto possui um único repositório Git ou repositórios separados claramente definidos para cada microsserviço que compartilham a mesma raiz conceitual?
- [x] **Plano de Ação:** 
  - Verificar se todo o código fonte está versionado no GitHub.
  - Confirmar que não há múltiplas bases de código rodando a mesma aplicação (ex: forks divergentes sendo usados em produção simultaneamente).

## 2. Dependencies (Dependências)
*Declare e isole explicitamente as dependências.*

- [x] **Verificação:** O projeto declara todas as suas dependências através de um gerenciador de pacotes (Maven/POM.xml)?
- [x] **Plano de Ação:** 
  - Revisar o `pom.xml` dos microsserviços. Foi verificado que o projeto utiliza o padrão do Spring Boot através do `spring-boot-starter-parent` e BOMs (Bill of Materials) como `spring-cloud-dependencies`. Isso significa que, embora algumas dependências não tenham a tag `<version>` explícita no POM local, suas versões exatas são garantidas, rastreadas e isoladas pelo Parent POM, cumprindo o princípio do 12-Factor App de "declaração completa e exata".
  - Comandos como `mvn clean install` são suficientes e 100% autossuficientes.

## 3. Config (Configurações)
*Armazene as configurações no ambiente.*

- [ ] **Verificação:** As configurações (credenciais, URLs de banco de dados) estão separadas do código fonte?
- [ ] **Plano de Ação:** 
  - Validar o uso do `config-server` recém-configurado.
  - Assegurar que credenciais sensíveis (ex: `GITHUB_TOKEN`, `DEEPSEEK_API_KEY`, senhas de banco de dados) não estão hardcoded no código fonte, mas sim providas via variáveis de ambiente.

## 4. Backing Services (Serviços de Apoio)
*Trate os serviços de apoio como recursos anexados.*

- [ ] **Verificação:** Bancos de dados (PostgreSQL/pgvector), mensageria (RabbitMQ) e outros serviços externos podem ser trocados sem alterar o código?
- [ ] **Plano de Ação:** 
  - Conferir se os serviços externos estão acessíveis via URL e credenciais configuráveis.
  - Testar a capacidade de substituir o banco de dados local por um gerido na nuvem apenas alterando as variáveis de ambiente.

## 5. Build, Release, Run (Construa, lance, execute)
*Separe estritamente os estágios de construção e execução.*

- [ ] **Verificação:** O processo de deploy está separado nos estágios de Build (gerar o `.jar`/imagem), Release (juntar com a configuração) e Run (executar o processo)?
- [ ] **Plano de Ação:** 
  - Revisar o `Dockerfile` e os pipelines de CI/CD para garantir que o artefato construído (`.jar`) é imutável.
  - Garantir que um novo deploy sempre gera um novo Release ID.

## 6. Processes (Processos)
*Execute a aplicação como um ou mais processos sem estado (stateless).*

- [ ] **Verificação:** Os microsserviços do SUPER-SYS são stateless e não armazenam estado na memória local entre requisições?
- [ ] **Plano de Ação:** 
  - Analisar os controllers e services para garantir que nenhum estado de sessão do usuário está sendo guardado em memória (`HashMap` estático, sessões HTTP locais).
  - Certificar-se de que o estado compartilhado está em um serviço de apoio (ex: banco de dados, Redis).

## 7. Port Binding (Vínculo de Portas)
*Exporte serviços através de vínculo de portas.*

- [ ] **Verificação:** A aplicação é autocontida e não depende de injeção em um servidor web externo (ex: Tomcat instalado no SO)?
- [ ] **Plano de Ação:** 
  - Validar se cada serviço do Spring Boot levanta seu próprio servidor embutido (Tomcat/Netty) e expõe os endpoints amarrados a uma porta definida (`server.port`).

## 8. Concurrency (Concorrência)
*Dimensione através do modelo de processos.*

- [ ] **Verificação:** A aplicação pode escalar horizontalmente apenas subindo novas instâncias do processo?
- [ ] **Plano de Ação:** 
  - Configurar e testar o `docker-compose.yml` e/ou Kubernetes para subir múltiplas réplicas do `ai-service` ou `api-gateway`.
  - Garantir que não há gargalos arquiteturais impedindo o balanceamento de carga.

## 9. Disposability (Descartabilidade)
*Maximize a robustez com inicialização rápida e desligamento gracioso (graceful shutdown).*

- [ ] **Verificação:** Os serviços podem iniciar rapidamente e serem encerrados de forma segura a qualquer momento?
- [ ] **Plano de Ação:** 
  - Habilitar e testar o Graceful Shutdown no Spring Boot (`server.shutdown=graceful`).
  - Assegurar que processos em andamento (ex: chamadas para a API do DeepSeek ou RabbitMQ) são tratados adequadamente durante o desligamento.

## 10. Dev/Prod Parity (Paridade entre Desenvolvimento e Produção)
*Mantenha os ambientes de desenvolvimento, teste e produção o mais semelhantes possível.*

- [ ] **Verificação:** O ambiente do desenvolvedor local é similar ao ambiente de produção?
- [ ] **Plano de Ação:** 
  - Utilizar Docker e `docker-compose.yml` para rodar dependências locais (PostgreSQL, pgvector, RabbitMQ, Config Server) espelhando a arquitetura de produção.
  - Minimizar diferenças nas versões das dependências externas.

## 11. Logs (Logs)
*Trate os logs como fluxo de eventos.*

- [ ] **Verificação:** A aplicação escreve logs para o fluxo de saída padrão (`stdout`) em vez de gerenciar arquivos de log diretamente?
- [ ] **Plano de Ação:** 
  - Revisar as configurações do Logback/SLF4J para garantir que os logs são direcionados para o console.
  - Garantir que o ambiente de execução (Docker, Kubernetes, ou ELK stack na pasta `observability`) é quem captura, roteia e armazena os logs.

## 12. Admin Processes (Processos de Administração)
*Execute tarefas de administração/gerenciamento como processos pontuais.*

- [ ] **Verificação:** Tarefas administrativas (migrações de banco de dados, scripts de manutenção) rodam no mesmo ambiente e versão de código que a aplicação principal?
- [ ] **Plano de Ação:** 
  - Implementar ferramentas como Flyway ou Liquibase para migrações de banco de dados automatizadas e embutidas.
  - Criar endpoints de administração protegidos ou usar tarefas agendadas nativas em vez de conexões diretas no banco de dados para manutenção.
