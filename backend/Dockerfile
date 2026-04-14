# syntax=docker/dockerfile:1

FROM gradle:9.3.1-jdk21 AS build
WORKDIR /app

COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Сначала прогреваем кэш зависимостей, затем копируем исходники.
RUN ./gradlew --no-daemon --console=plain --stacktrace --info dependencies || true

COPY src ./src

RUN ./gradlew --no-daemon --console=plain --stacktrace --info bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /opt/app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]