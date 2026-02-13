package com.snzh.config;

import com.snzh.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author haibara
 * @description Web MVC配置
 * @since 2025/10/11
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                // 拦截所有请求
                .addPathPatterns("/**")
                // 放行登录接口
                .excludePathPatterns(
                        "/user/wx/login",           // 小程序登录
                        "/user/refresh",            // 用户刷新Token
                        "/admin/login",             // 管理员登录
                        "/admin/refresh",           // 管理员刷新Token
                        "/ai/test/**",              // AI测试接口（无需登录）
                        "/swagger-ui/**",           // Swagger UI
                        "/swagger-ui.html",
                        "/v3/api-docs/**",          // API文档
                        "/doc.html",                // Knife4j文档
                        "/favicon.ico",             // 网站图标
                        "/error"                    // 错误页面
                );
    }
}

