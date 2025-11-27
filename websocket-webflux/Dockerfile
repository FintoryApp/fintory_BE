FROM eclipse-temurin:17-jre-alpine AS builder
WORKDIR /app

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./

COPY websocket-webflux/build.gradle ./websocket-webflux/

RUN ./gradlew --no-daemon dependencies || true

COPY websocket-webflux/src ./websocket-webflux/src

RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
  ./gradlew :websocket-webflux:bootJar --no-daemon --build-cache -x test

FROM eclipse-temurin:17-jre-alpine

ENV TZ=Asia/Seoul
ENV JAVA_OPTS="-XX:ReservedCodeCacheSize=128m -XX:MaxMetaspaceSize=200m -XX:+UseG1GC -Xms350m -Xmx350m -XX:NativeMemoryTracking=summary -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/jvm-dump.hprof"

WORKDIR /app

COPY --from=builder /app/websocket-webflux/build/libs/*.jar app.jar

ENTRYPOINT ["java $JAVA_OPTS -jar app.jar", "--spring.profiles.active=deploy-webflux"]
