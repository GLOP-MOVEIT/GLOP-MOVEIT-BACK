# Stage 1: Build
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

# Copy parent pom first
COPY pom.xml .

# Copy localization module
COPY localization localization

# Build the application
WORKDIR /workspace/localization
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Install wget for healthcheck
RUN apk add --no-cache wget

# Copy the jar from build stage
COPY --from=build /workspace/localization/target/*.jar app.jar

# Expose port (optionnel, à commenter si non exposé)
# EXPOSE 8085

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
