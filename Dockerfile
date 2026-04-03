FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user and set ownership of /app directory
RUN addgroup -S spring && adduser -S spring -G spring && \
  chown -R spring:spring /app
USER spring:spring

# Copy the JAR from local target directory
COPY --chown=spring:spring target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=300s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

