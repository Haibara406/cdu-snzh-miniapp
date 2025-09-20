package com.snzh.exceptions;

/**
 * @author haibara
 * @description 账户禁用异常
 * @since 2025/9/20 23:00
 */
public class AccountLockedException extends RuntimeException {
    public AccountLockedException(String message) {
        super(message);
    }
}
