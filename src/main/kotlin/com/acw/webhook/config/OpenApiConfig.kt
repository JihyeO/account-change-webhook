package com.acw.webhook.config

import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.security.SecurityRequirement
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun customOpenAPI(): OpenAPI {
        // Add header-based API Key security schemes so Swagger UI allows entering X-Signature / X-Event-Id
        val components = Components()
            .addSecuritySchemes(
                "X-Signature",
                SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .`in`(SecurityScheme.In.HEADER)
                    .name("X-Signature")
            )
            .addSecuritySchemes(
                "X-Event-Id",
                SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .`in`(SecurityScheme.In.HEADER)
                    .name("X-Event-Id")
            )

        // Do not add a global SecurityRequirement here; keep schemes in components
        // so per-operation @Parameter header annotations are displayed in Swagger UI.
        return OpenAPI()
            .components(components)
            .info(
                Info()
                    .title("Account Change Webhook API")
                    .version("v0.0.1")
                    .description("API for receiving and inspecting account change webhooks")
                    .contact(Contact().name("ACW Team"))
            )
    }
}
