FROM ubuntu:latest
LABEL authors="Noel"

ENTRYPOINT ["top", "-b"]

FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .

COPY src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:17-jre-jammy

ARG JAR_FILE=/app/target/*.jar

WORKDIR /app

COPY --from=builder ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]