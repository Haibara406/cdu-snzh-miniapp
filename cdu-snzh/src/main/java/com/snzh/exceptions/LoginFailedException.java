package com.snzh.exceptions;

/**
 * @author haibara
 * @description 登陆失败异常
 * @since 2025/9/20 23:17
 */
public class LoginFailedException extends RuntimeException {
    public LoginFailedException(String message) {
        super(message);
    }
}
