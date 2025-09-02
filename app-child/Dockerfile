FROM eclipse-temurin:17-jdk
RUN apk update && apk add --no-cache \
    bash \
    udev \
    ttf-freefont \
    chromium \
    chromium-chromedriver \
    && rm -rf /var/cache/apk/*

ENV CHROME_BIN=/usr/lib/chromium/chrome
ENV PATH="$PATH:/usr/lib/chromium/"

WORKDIR /app

COPY build/libs/*.jar app.jar
COPY src/main/resources/application-deploy.yml /app/application-deploy.yml

ENTRYPOINT ["java", "-jar","app.jar", "--spring.profiles.active=deploy"]
