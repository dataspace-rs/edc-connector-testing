FROM eclipse-temurin:24.0.1_9-jre-alpine

ARG JVM_ARGS=""
ARG JAR

WORKDIR /app
COPY ${JAR} controlplane.jar

ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:1044,", "-jar", "controlplane.jar"]
