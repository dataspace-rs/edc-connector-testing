FROM eclipse-temurin:24.0.1_9-jre-alpine

ARG JVM_ARGS=""
ARG JAR

WORKDIR /app
COPY ${JAR} controlplane-dcp.jar

ENV ENV_JVM_ARGS=$JVM_ARGS
ENTRYPOINT [ "sh", "-c", "exec java $ENV_JVM_ARGS -jar controlplane-dcp.jar" ]
