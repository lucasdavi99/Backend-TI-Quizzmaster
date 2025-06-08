# Backend Dockerfile Melhorado
FROM maven:3.9.9-eclipse-temurin-17 AS builder

# Cria diretório de trabalho
WORKDIR /app

# Copia apenas pom.xml primeiro para aproveitar cache do Docker
COPY pom.xml .

# Baixa dependências (aproveitará cache se pom.xml não mudar)
RUN mvn dependency:go-offline -B

# Copia código fonte
COPY src ./src

# Compila aplicação
RUN mvn clean package -DskipTests

# Stage de runtime - usando JRE ao invés de JDK (menor)
FROM eclipse-temurin:17-jre-alpine

# Instala curl para healthcheck
RUN apk add --no-cache curl

# Cria usuário não-root
RUN addgroup -g 1001 spring && adduser -u 1001 -G spring -s /bin/sh -D spring

# Cria diretório da aplicação
RUN mkdir /app && chown spring:spring /app

# Copia JAR da aplicação
COPY --from=builder --chown=spring:spring /app/target/*.jar /app/app.jar

# Muda para usuário não-root
USER spring

# Define diretório de trabalho
WORKDIR /app

# Expõe porta
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando de inicialização
CMD ["java", "-jar", "app.jar"]