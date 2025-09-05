package com.fintory.infra.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Bean
    public OpenAPI customOpenAPI() {

        OpenAPI openAPI = new OpenAPI();

        if (activeProfile.equals("deploy")) {
            openAPI.addServersItem(new Server().url("https://fintory.xyz"));
        }

        return openAPI
                .addSecurityItem(new SecurityRequirement().addList("Authorization")) // 기본값: 모든 api 보안 필요
                .components(new Components()
                        .addSecuritySchemes("Authorization",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }
}
