
package com.cts.foodchainx.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAPI (Swagger) documentation.
 * Enables the 'Authorize' button in Swagger UI to support JWT Bearer tokens.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI foodChainOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("FoodChainX API Documentation")
                .version("1.0")
                .description("API endpoints for the FoodChainX Farm-to-Table transparency system."))
            // This adds the 'Authorize' button globally to the UI
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .bearerFormat("JWT")
            .scheme("bearer")
            .description("Enter your JWT token in the format: <token_only>");
    }
}