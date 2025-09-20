package com.snzh.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author haibara
 * @description 接口文档配置
 * @since 2025/7/27 16:32
 */

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("CDU SNZH BACKEND API")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Haibara")
                                .email("haibara406@gmail.com")
                                .email("haibaraiii@foxmail.com")
                                .url("https://github.com/Haibara406")
                                .url("https://gitee.com/haibaraiii"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
