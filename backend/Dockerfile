# syntax=docker/dockerfile:1

FROM gradle:9.3.1-jdk21 AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY src src

RUN gradle --no-daemon bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /opt/app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]

