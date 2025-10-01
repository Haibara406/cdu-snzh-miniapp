package com.snzh.exceptions;

/**
 * @author haibara
 * @description 门票类型不存在异常
 * @since 2025/9/28 18:53
 */
public class TicketTypeNotFoundException extends RuntimeException {
    public TicketTypeNotFoundException(String message) {
        super(message);
    }
}
