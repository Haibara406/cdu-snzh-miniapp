package com.snzh.exceptions;

/**
 * @author haibara
 * @description 账户不存在异常
 * @since 2025/9/20 23:28
 */
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
