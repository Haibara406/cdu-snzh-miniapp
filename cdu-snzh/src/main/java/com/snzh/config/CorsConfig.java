package com.snzh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author haibara
 * @description 跨域配置
 * @since 2025/10/11
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许所有来源访问（开发环境）
        config.addAllowedOriginPattern("*");
        
        // 允许携带凭证（Cookie、Authorization header 等）
        config.setAllowCredentials(true);
        
        // 允许所有请求头
        config.addAllowedHeader("*");
        
        // 允许所有HTTP方法（GET、POST、PUT、DELETE、OPTIONS等）
        config.addAllowedMethod("*");
        
        // 预检请求的有效期，单位为秒（避免频繁发送 OPTIONS 请求）
        config.setMaxAge(3600L);
        
        // 允许浏览器访问的响应头
        config.addExposedHeader("Content-Disposition");
        config.addExposedHeader("Authorization");
        
        // 配置跨域规则应用到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}

