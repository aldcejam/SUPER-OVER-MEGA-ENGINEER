# Guia de Implementação de Observabilidade com OpenTelemetry (OTel)

Este guia serve como um tutorial completo para replicar a stack de observabilidade implementada no projeto SUPER-SYS em qualquer outro projeto Java/Spring Boot.

A arquitetura baseia-se em **Agentes** nas aplicações que enviam dados de telemetria (Logs, Métricas e Traces) para um **Collector Central**, que por sua vez distribui esses dados para bancos de dados especializados.

## 🏗️ A Stack (O que compõe a solução?)

1.  **OpenTelemetry Java Agent**: Instrumenta a aplicação Java automaticamente, sem precisar alterar o código-fonte, capturando chamadas HTTP, banco de dados, logs, etc.
2.  **OpenTelemetry Collector**: O "carteiro". Recebe todos os dados da aplicação via protocolo OTLP e entrega para os destinos corretos.
3.  **Prometheus**: Armazena as **Métricas** (CPU, memória, latência, throughput).
4.  **Tempo**: Armazena os **Traces** (o caminho e o tempo que uma requisição levou passando por vários microsserviços).
5.  **Loki**: Armazena os **Logs** da aplicação.
6.  **Grafana**: A interface visual (Dashboard) que conecta todas essas fontes e permite cruzar as informações (ex: ver os logs de um trace específico).

---

## 🛠️ Passo 1: Subindo a Infraestrutura (Docker Compose)

Em seu arquivo `docker-compose.yml`, você precisará declarar os serviços essenciais. 

> [!IMPORTANT]  
> **Rede Docker**: É crítico que todas as ferramentas de observabilidade e as suas aplicações estejam na **mesma rede docker** (ex: `minha-rede-interna`), para que os containers possam se comunicar usando seus nomes (ex: `http://otel-collector:4318`).

### Exemplo de Serviços Básicos no `docker-compose.yml`:

```yaml
  otel-collector:
    image: otel/opentelemetry-collector-contrib:latest
    volumes:
      - ./observability/otel-collector/config.yaml:/etc/otel-collector/config.yaml
    ports:
      - "4318:4318" # Porta HTTP para receber dados das aplicações
    networks:
      - minha-rede-interna

  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./observability/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - "9090:9090"
    networks:
      - minha-rede-interna

  tempo:
    image: grafana/tempo:latest
    command: [ "-config.file=/etc/tempo.yaml" ]
    volumes:
      - ./observability/tempo/tempo.yml:/etc/tempo.yaml
    ports:
      - "3200:3200"
    networks:
      - minha-rede-interna

  loki:
    image: grafana/loki:latest
    ports:
      - "3100:3100"
    # O Loki já vem com uma configuração padrão aceitável para desenvolvimento
    networks:
      - minha-rede-interna

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_AUTH_ANONYMOUS_ENABLED=true # Acesso sem login (opcional)
      - GF_AUTH_ANONYMOUS_ORG_ROLE=Admin
    volumes:
      - ./observability/grafana/datasources.yml:/etc/grafana/provisioning/datasources/datasources.yml
    networks:
      - minha-rede-interna
```

---

## ⚙️ Passo 2: Configurando os Componentes

Crie uma pasta chamada `observability` na raiz do seu projeto para organizar as configurações.

### 2.1 OTel Collector (`observability/otel-collector/config.yaml`)
O Collector precisa saber de onde recebe (receivers) e para onde manda (exporters) os dados.

```yaml
receivers:
  otlp:
    protocols:
      http:
        endpoint: 0.0.0.0:4318 # Aplicações mandarão para cá

exporters:
  prometheus:
    endpoint: "0.0.0.0:8889" # O Prometheus vai "raspar" (scrape) dessa porta
    namespace: "meu-projeto"
    
  otlp/tempo:
    endpoint: "tempo:4317" # Envia traces pro Tempo via gRPC
    tls:
      insecure: true

  otlphttp/loki:
    endpoint: "http://loki:3100/otlp" # Envia logs pro Loki via OTLP nativo (Loki 3.x)
    tls:
      insecure: true

  debug:
    verbosity: detailed # Útil para ver no console do docker se está recebendo dados

processors:
  batch:

service:
  pipelines:
    traces:
      receivers: [otlp]
      processors: [batch]
      exporters: [otlp/tempo, debug]
    metrics:
      receivers: [otlp]
      processors: [batch]
      exporters: [prometheus, debug]
    logs:
      receivers: [otlp]
      processors: [batch]
      exporters: [otlphttp/loki, debug]
```

### 2.2 Prometheus (`observability/prometheus/prometheus.yml`)
Configurado para raspar as métricas que o OTel Collector disponibilizou.

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'otel-collector'
    static_configs:
      - targets: ['otel-collector:8889']
```

### 2.3 Tempo (`observability/tempo/tempo.yml`)
> [!WARNING]  
> É **crucial** configurar os endpoints como `0.0.0.0`, senão o Tempo escutará apenas internamente (localhost) e o OTel Collector receberá o erro `Connection Refused`.

```yaml
server:
  http_listen_port: 3200

distributor:
  receivers:
    otlp:
      protocols:
        http:
          endpoint: 0.0.0.0:4318
        grpc:
          endpoint: 0.0.0.0:4317 # Porta que o OTel Collector vai chamar

storage:
  trace:
    backend: local
    local:
      path: /tmp/tempo/blocks
```

### 2.4 Grafana (`observability/grafana/datasources.yml`)
Este arquivo cadastra automaticamente o Prometheus, Tempo e Loki no Grafana.
O segredo da correlação **Trace -> Log** fica no bloco `tracesToLogsV2` do Tempo.

```yaml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    uid: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true

  - name: Loki
    type: loki
    uid: loki
    access: proxy
    url: http://loki:3100

  - name: Tempo
    type: tempo
    uid: tempo
    access: proxy
    url: http://tempo:3200
    jsonData:
      nodeGraph:
        enabled: true
      tracesToLogsV2:
        datasourceUid: 'loki' # Vincula ao Loki
        tags: ['job', 'instance', 'pod', 'namespace']
        mappedTags: [{ key: 'service.name', value: 'service' }] # Mapeia tags entre Trace e Log
        mapTagNamesEnabled: false
        spanStartTimeShift: '1h'
        spanEndTimeShift: '-1h'
        filterByTraceID: false
        filterBySpanID: false
```

---

## 🚀 Passo 3: Instrumentando sua Aplicação Spring Boot

Para que sua aplicação gere dados, ela precisa do **OpenTelemetry Java Agent**. Baixe a última versão do `opentelemetry-javaagent.jar` (do GitHub oficial) e salve no seu projeto.

Você pode rodar sua aplicação de duas formas:

### Opção A: Rodando Localmente pela IDE (IntelliJ / Eclipse)
Configure o Run/Debug da sua aplicação passando as opções da máquina virtual (VM Options) e as variáveis de ambiente.

1. **VM Options**: 
   `-javaagent:/caminho/absoluto/na/sua/maquina/opentelemetry-javaagent.jar`
2. **Environment Variables**:
   `OTEL_SERVICE_NAME=meu-microservico`
   `OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318` *(Aponta para localhost porque o Docker exportou essa porta)*
   `OTEL_METRICS_EXPORTER=otlp`
   `OTEL_TRACES_EXPORTER=otlp`
   `OTEL_LOGS_EXPORTER=otlp`

### Opção B: Rodando no Docker (via Dockerfile)

A forma mais limpa de injetar o agente sem quebrar aplicações que não o utilizam é usar um curinga na cópia do Dockerfile e habilitá-lo via variáveis de ambiente no Compose.

**No seu `Dockerfile` genérico:**
```dockerfile
# O curinga (*) faz a cópia ser opcional. 
# Se o jar estiver na pasta do contexto do serviço, ele copia. Senão, ignora e o build não falha.
COPY opentelemetry-javaagent.jar* /app/

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**No seu `docker-compose.yml` (para o microserviço):**
```yaml
  meu-microservico:
    build:
      context: ./pasta-do-servico
      dockerfile: ../Dockerfile
    environment:
      # O Java nativamente lê a variável JAVA_TOOL_OPTIONS e injeta no comando de inicialização!
      JAVA_TOOL_OPTIONS: "-javaagent:/app/opentelemetry-javaagent.jar"
      OTEL_SERVICE_NAME: "meu-microservico"
      OTEL_EXPORTER_OTLP_ENDPOINT: "http://otel-collector:4318" # Aqui usamos o nome do container
      OTEL_METRICS_EXPORTER: "otlp"
      OTEL_TRACES_EXPORTER: "otlp"
      OTEL_LOGS_EXPORTER: "otlp"
```
*(Não esqueça de colocar o arquivo `opentelemetry-javaagent.jar` na pasta `./pasta-do-servico` para o Dockerfile conseguir copiá-lo).*

---

## 🎉 Testando
1. Suba os containers da observabilidade (`docker compose up -d`).
2. Suba a aplicação (na IDE ou no Docker).
3. Faça algumas requisições na aplicação.
4. Acesse o Grafana em `http://localhost:3000`.
5. Vá em **Explore**, escolha o DataSource **Tempo**, busque por um Trace.
6. Ao abrir o Trace, clique no ícone de "documento" ao lado de um Span para ver os Logs correspondentes puxados automaticamente do **Loki**!
