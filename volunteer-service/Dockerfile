# Stage 1: Build
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

# Copy parent pom first
COPY pom.xml .

# Copy volunteer-service module
COPY volunteer-service volunteer-service

# Build the application
WORKDIR /workspace/volunteer-service
RUN mvn clean package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Install wget for healthcheck
RUN apk add --no-cache wget

# Copy the jar from build stage
COPY --from=build /workspace/volunteer-service/target/*.jar app.jar

# Expose port
EXPOSE 8085

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
