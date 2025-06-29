FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY build/libs/*SNAPSHOT.jar fintory.jar
COPY src/main/resources/application-ci.yml ./config/application-ci.yml
ENTRYPOINT ["java", "-jar","fintory.jar", "--spring.profiles.active=ci"]
