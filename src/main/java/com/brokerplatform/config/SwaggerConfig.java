package com.brokerplatform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private int port;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:" + port + "/api").description("Desenvolvimento"),
                        new Server().url("https://api.brokerplatform.com").description("Produção")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Insira o token JWT obtido no endpoint /auth/login")));
    }

    private Info apiInfo() {
        return new Info()
                .title("Broker Platform API")
                .description("""
                        API REST para plataforma de gestão de apólices de seguro para corretores.

                        **Seguradoras integradas:**
                        - Icatu (integração real)
                        - Azos (mockado — integração futura)
                        - Mongeral/MAG (mockado — integração futura)

                        **Autenticação:** JWT Bearer Token — obtenha via `POST /auth/login`
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Broker Platform Team")
                        .email("dev@brokerplatform.com"))
                .license(new License().name("Proprietário").url("#"));
    }
}
