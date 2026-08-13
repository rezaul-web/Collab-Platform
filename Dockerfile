FROM gradle:8.7-jdk21 AS builder
WORKDIR /app
COPY . .
ARG MODULE
RUN gradle :${MODULE}:build -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ARG MODULE
# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
# Copy the built jar
COPY --from=builder /app/${MODULE}/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
