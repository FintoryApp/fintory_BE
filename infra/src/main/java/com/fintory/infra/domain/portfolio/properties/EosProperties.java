package com.fintory.infra.domain.portfolio.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "eos")
@Getter
public class EosProperties {
    private String apiKey;
}
