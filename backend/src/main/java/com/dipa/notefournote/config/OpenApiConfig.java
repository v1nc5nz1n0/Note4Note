package com.dipa.notefournote.config; // Assicurati che il package sia corretto

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        final String securitySchemeName = "bearerAuth";
        final SecurityScheme securityScheme = new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Autenticazione tramite JWT Bearer Token. Inserire il token preceduto da 'Bearer '.");

        final Info apiInfo = new Info()
                .title("Note4Note API")
                .version("0.0.1")
                .description("Web application per la gestione di un sistema di note condivise")
                .contact(new Contact()
                        .name("Vincenzo Caputo")
                        .email("vincenzo.caputo@hotmail.com"))
                .license(new License()
                        .name("Restricted - For Assessment Review Only")
                        .url("https://github.com/v1nc5nz1n0/Note4Note/blob/main/README.md"));

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components().addSecuritySchemes(securitySchemeName, securityScheme))
                .info(apiInfo);
    }

}