FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn clean package

FROM eclipse-temurin:25-jre
RUN apt-get update -y && apt-get install -y osmium-tool
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]