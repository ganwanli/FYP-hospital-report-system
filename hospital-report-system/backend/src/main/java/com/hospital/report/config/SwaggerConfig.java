package com.hospital.report.config;

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

import java.util.Arrays;

/**
 * Swagger API文档配置
 * 
 * @author Hospital Report System
 * @since 2024-01-01
 */
@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * OpenAPI配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080").description("开发环境"),
                        new Server().url("https://api.hospital.com").description("生产环境")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }

    /**
     * API信息
     */
    private Info apiInfo() {
        return new Info()
                .title("医院报表管理系统 API")
                .description("医院报表管理系统后端API接口文档")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Hospital Report System Team")
                        .email("support@hospital.com")
                        .url("https://www.hospital.com")
                )
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                );
    }
}