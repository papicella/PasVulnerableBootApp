FROM openjdk:17-jre-slim

RUN groupadd -r appgroup && useradd -r -g appgroup appuser

COPY target/PasVulnerableBootApp-0.0.1-SNAPSHOT.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]