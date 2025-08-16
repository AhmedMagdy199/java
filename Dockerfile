# Stage 1: Build the application
FROM maven:3.9.3-openjdk-11 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Build the JAR
RUN mvn clean package -DskipTests

# Stage 2: Create minimal runtime image
FROM openjdk:11-jre-slim

# Set working directory
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/demo1-0.0.1-SNAPSHOT.jar /app/demo1-0.0.1-SNAPSHOT.jar

# Run the application
ENTRYPOINT ["java", "-jar", "demo1-0.0.1-SNAPSHOT.jar"]

