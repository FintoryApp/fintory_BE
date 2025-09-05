package com.fintory.infra.domain.portfolio.properties;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "eos")
@Data
public class EosProperties {
    private String apiKey;
}
