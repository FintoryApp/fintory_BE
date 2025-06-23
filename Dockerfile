FROM eclipse-temurin:17-jdk-alpine
COPY build/libs/*SNAPSHOT.jar fintory.jar
ENTRYPOINT ["java", "-jar","fintory.jar"]