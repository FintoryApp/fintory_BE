package com.fintory.parent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaAuditing
@SpringBootApplication(scanBasePackages = {
        "com.fintory.infra",
        "com.fintory.auth",
        "com.fintory.parent"
})
@ConfigurationPropertiesScan(basePackages = {
        "com.fintory.auth"
})
@EntityScan(basePackages = "com.fintory.domain")
@EnableJpaRepositories(basePackages = "com.fintory.infra")
class ParentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ParentApplication.class, args);
    }
}
