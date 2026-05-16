# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy dependency descriptor first for layer caching
COPY pom.xml .
RUN mvn dependency:resolve -q --no-transfer-progress

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -q --no-transfer-progress

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd -r nextpay && useradd -r -g nextpay nextpay
USER nextpay

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
