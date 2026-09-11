package org.example.bankappcardservice.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cardServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Bank App - Card Service")
                .description("Virtual card generation and lifecycle")
                .version("v1"));
    }
}