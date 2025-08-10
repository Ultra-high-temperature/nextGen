package com.example.nextgen.common.chatmodel;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NextGen API")
                        .version("1.0")
                        .description("NextGen 项目 API 文档")
                        .contact(new Contact()
                                .name("NextGen Team")
                                .email("nextgen@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8899")
                                .description("本地开发服务器"),
                        new Server()
                                .url("http://139.199.156.222:8899")
                                .description("开发服务器")
                ));
    }
}