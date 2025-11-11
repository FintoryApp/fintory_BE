package com.fintory.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;



@EnableScheduling
@SpringBootApplication(scanBasePackages = {
        "com.fintory.websocket",
        "com.fintory.domain",
        "com.fintory.common",
        "com.fintory.infra.config",
        "com.fintory.infra.domain.stock.service.token"
})
@EntityScan(basePackages = "com.fintory.domain")
@EnableJpaRepositories(
        basePackages = {
                "com.fintory.websocket.publisher.repository"
        }
)
public class WebsocketApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebsocketApplication.class, args);
    }
}
