package com.snzh.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author haibara
 * @description 密码加密工具类
 * @since 2025/10/11
 */
public class PasswordUtils {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(10);

    /**
     * 密码加密
     */
    public static String encode(String rawPassword) {
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    /**
     * 密码验证
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }
}

