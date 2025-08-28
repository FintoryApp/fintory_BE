FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY build/libs/*.jar app.jar
COPY application-deploy.yml /app/application-deploy.yml

ENTRYPOINT ["java", "-jar","app.jar", "--spring.profiles.active=deploy"]
