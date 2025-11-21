package com.fintory.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;



@EnableScheduling
@SpringBootApplication(scanBasePackages = {
        "com.fintory.websocket",
        "com.fintory.domain",
        "com.fintory.common",
        "com.fintory.infra.config", //TODO infra 모듈의 경우 로컬 실행을 위해서 남겨둠 -> 최종 때 삭제 예정
        "com.fintory.infra.domain.stock.service.token"
})
@EntityScan(basePackages = "com.fintory.domain")
@EnableJpaRepositories(
        basePackages = {
                "com.fintory.websocket.publisher.repository"
        }
)
@EnableJpaAuditing
public class WebsocketApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebsocketApplication.class, args);
    }
}
