# ==========================================
# Step 1: Build Stage (Maven + OpenJDK 17)
# ==========================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package the application as a WAR (skipping unit tests for fast build)
RUN mvn clean package -DskipTests

# ==========================================
# Step 2: Runtime Stage
# ==========================================
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the generated WAR file from the build stage
# Maven generates the WAR in the /target folder
COPY --from=build /app/target/*.war app.war

# Render automatically exposes port 8080 by default
EXPOSE 8080

# Spring Boot WARs with embedded Tomcat run directly via java -jar
ENTRYPOINT ["java", "-jar", "app.war"]