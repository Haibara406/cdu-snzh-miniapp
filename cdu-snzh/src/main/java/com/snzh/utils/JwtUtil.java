package com.snzh.utils;

import com.snzh.domain.properties.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author haibara
 * @description jwt工具类
 * @since 2025/9/20 11:44
 */

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class JwtUtil {

    private final JwtProperties jwtProperties;

    private final String secret = jwtProperties.getSecret();

    private final long accessTokenExpire = jwtProperties.getAccessTokenExpire();

    private final long refreshTokenExpire = jwtProperties.getRefreshTokenExpire();

    private SecretKey key;

    @PostConstruct
    public void init() {
        // 初始化密钥
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成 Access Token
     */
    public String generateAccessToken(String userId, String status) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("status", status);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpire))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成 Refresh Token
     */
    public String generateRefreshToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpire))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 校验 Token 并解析
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token 已过期", e);
        } catch (JwtException e) {
            throw new RuntimeException("Token 非法", e);
        }
    }

    /**
     * 判断 Token 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public String getUserId(String token) {
        return parseToken(token).get("userId", String.class);
    }

    public String getStatus(String token) {
        return parseToken(token).get("status", String.class);
    }
}

