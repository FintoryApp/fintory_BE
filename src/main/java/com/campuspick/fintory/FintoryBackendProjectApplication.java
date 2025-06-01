package com.campuspick.fintory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class FintoryBackendProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(FintoryBackendProjectApplication.class, args);
	}

}
