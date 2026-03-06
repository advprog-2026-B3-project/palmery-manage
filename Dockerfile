FROM gradle:9.3.0-jdk21-alpine AS builder

WORKDIR /app
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew
COPY src ./src
RUN ./gradlew --no-daemon bootJar
RUN ls build/libs/*.jar | grep -v -- '-plain\.jar$' | head -n 1 | xargs -I{} cp "{}" /app/app.jar

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=builder /app/app.jar /app/app.jar
EXPOSE 8081
ENV SERVER_PORT=8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
