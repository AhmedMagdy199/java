# Stage 1: Build the application
FROM eclipse-temurin:11-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal runtime image
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/demo1-0.0.1-SNAPSHOT.jar /app/demo1-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "demo1-0.0.1-SNAPSHOT.jar"]
