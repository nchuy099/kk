FROM gradle:8.10.2-jdk21 AS build
WORKDIR /workspace
ARG SERVICE_DIR
COPY . .
RUN gradle :${SERVICE_DIR}:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
ARG SERVICE_DIR
COPY --from=build /workspace/${SERVICE_DIR}/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

