FROM gradle:8.5-jdk17-alpine AS builder
WORKDIR /app

COPY gradlew gradlew
COPY gradle/wrapper gradle/wrapper
COPY build.gradle settings.gradle ./

COPY websocket-webflux/build.gradle ./websocket-webflux/

RUN ./gradlew dependencies --no-daemon

COPY websocket-webflux/src ./websocket-webflux/src

RUN ./gradlew :websocket-webflux:bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/websocket-webflux/build/libs/*.jar app.jar

COPY websocket-webflux/src/main/resources/application-deploy-webflux.yml /app/application-deploy-webflux.yml

ENTRYPOINT ["java", \
     "-XX:ReservedCodeCacheSize=128m", \
     "-XX:MaxMetaspaceSize=200m", \
     "-XX:+UseG1GC", \
     "-Xms350m", "-Xmx350m", \
     "-XX:NativeMemoryTracking=summary", \
     "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=/tmp/jvm-dump.hprof", \
     "-jar","app.jar", \
     "--spring.profiles.active=deploy-webflux"]
