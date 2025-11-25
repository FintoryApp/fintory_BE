FROM eclipse-temurin:17-jdk

RUN apt-get update && apt-get install -y tzdata \
    && rm -rf /var/lib/apt/lists/*

ENV TZ=Asia/Seoul

WORKDIR /app

COPY build/libs/*.jar app.jar
COPY src/main/resources/application-deploy-webflux.yml /app/application-deploy-webflux.yml

ENTRYPOINT ["java", "-Xms700m", "-Xmx700m", "-jar","app.jar", "--spring.profiles.active=deploy-webflux"]
