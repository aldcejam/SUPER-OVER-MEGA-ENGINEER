# ============================================
# Dockerfile Multi-Stage para Serviços Spring Boot
# Usado por todos os microsserviços Java do SUPER-SYS
# ============================================

# --- Stage 1: Build ---
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copia apenas o pom.xml primeiro para cachear dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte e builda
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia o JAR gerado do stage de build
COPY --from=build /app/target/*.jar app.jar

# Porta exposta (será sobrescrita por cada serviço)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
