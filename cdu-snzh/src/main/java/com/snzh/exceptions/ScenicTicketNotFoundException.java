package com.snzh.exceptions;

/**
 * @author haibara
 * @description 景点门票不存在异常
 * @since 2025/9/29 16:55
 */
public class ScenicTicketNotFoundException extends RuntimeException {
    public ScenicTicketNotFoundException(String message) {
        super(message);
    }
}
