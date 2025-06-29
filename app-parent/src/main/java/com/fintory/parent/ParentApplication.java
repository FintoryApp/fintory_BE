package com.fintory.parent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.fintory.infra",
        "com.fintory.parent"
})
@EntityScan(basePackages = "com.fintory.domain")
@EnableJpaRepositories(basePackages = "com.fintory.domain")
@EnableJpaAuditing
class ParentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParentApplication.class, args);
    }

}
