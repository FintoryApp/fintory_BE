FROM eclipse-temurin:17-jdk
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    libnss3 \
    libxss1 \
    libasound2 \
    libatk-bridge2.0-0 \
    libgtk-3-0 \
    gnupg \
    unzip \
    chromium \
    chromium-driver \
    fonts-freefont-ttf \
    --no-install-recommends && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

ENV CHROME_BIN=/usr/lib/chromium/chrome
ENV PATH="$PATH:/usr/lib/chromium/"

WORKDIR /app

COPY build/libs/*.jar app.jar
COPY src/main/resources/application-deploy.yml /app/application-deploy.yml

ENTRYPOINT ["java", "-jar","app.jar", "--spring.profiles.active=deploy"]
