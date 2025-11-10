package com.fintory.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;



@EnableScheduling
@SpringBootApplication(scanBasePackages = {
        "com.fintory.websocket",
        "com.fintory.domain",
        "com.fintory.common",
        "com.fintory.infra"
})
@EnableJpaRepositories(basePackages = {
        "com.fintory.websocket.publisher.repository"
})
public class WebsocketApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebsocketApplication.class, args);
    }
}
