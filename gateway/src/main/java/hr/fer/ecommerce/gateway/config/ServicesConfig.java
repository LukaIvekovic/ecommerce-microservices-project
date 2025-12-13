package hr.fer.ecommerce.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "service")
@Data
public class ServicesConfig {
    private ServiceConfig product;
    private ServiceConfig order;
    private ServiceConfig payment;
    private ServiceConfig shipping;

    @Data
    public static class ServiceConfig {
        private String url;
    }
}

