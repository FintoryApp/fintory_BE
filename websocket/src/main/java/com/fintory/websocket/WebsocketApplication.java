package com.fintory.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;



@EnableScheduling
@SpringBootApplication(scanBasePackages = {
        "com.fintory.websocket",
        "com.fintory.domain",
        "com.fintory.common",
        "com.fintory.infra"
})
public class WebsocketApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebsocketApplication.class, args);
    }
}
