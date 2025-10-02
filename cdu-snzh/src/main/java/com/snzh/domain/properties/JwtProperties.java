package com.snzh.domain.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author haibara
 * @description jwt属性配置
 * @since 2025/9/20 15:53
 */

@Component
@ConfigurationProperties(prefix = "snzh.jwt")
@Data
public class JwtProperties {

    @Value("${snzh.jwt.secret}")
    private String secret;

    @Value("${snzh.jwt.access-token-expire}")
    private long accessTokenExpire;

    @Value("${snzh.jwt.refresh-token-expire}")
    private long refreshTokenExpire;

}
