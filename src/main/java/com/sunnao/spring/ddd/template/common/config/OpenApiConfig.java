package com.sunnao.spring.ddd.template.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置
 * <p>
 * 文档地址：/swagger-ui.html（UI）、/v3/api-docs（JSON）；
 * 鉴权方式：请求头携带 satoken（与 sa-token.token-name 一致），
 * 在 Swagger UI 右上角 Authorize 中填入登录返回的 token 即可调试需登录接口。
 */
@Configuration
public class OpenApiConfig {

    /**
     * 与 sa-token.token-name 配置保持一致
     */
    private static final String TOKEN_HEADER = "satoken";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring DDD Template API")
                        .description("Spring Boot DDD 脚手架接口文档。除登录接口外均需在请求头携带 satoken。")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(TOKEN_HEADER, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(TOKEN_HEADER)
                                .description("登录接口返回的 token")))
                .addSecurityItem(new SecurityRequirement().addList(TOKEN_HEADER));
    }
}
