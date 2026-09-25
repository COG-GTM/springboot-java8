# Runtime base image is pinned by infra/terraform (var.runtime_image) and infra/.env (RUNTIME_IMAGE).
ARG BASE_IMAGE=eclipse-temurin:11-jre
FROM ${BASE_IMAGE}

LABEL com.datadoghq.tags.service="topics-api" \
      com.datadoghq.tags.env="legacy" \
      com.datadoghq.tags.version="0.1.0"

WORKDIR /app
COPY target/app.jar /app/app.jar
ENV JAVA_OPTS="-Xms128m -Xmx256m"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
