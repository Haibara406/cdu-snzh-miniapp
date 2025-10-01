package com.snzh.exceptions;

/**
 * @author haibara
 * @description 门票类型已经存在异常
 * @since 2025/9/28 18:05
 */
public class TicketTypeHasExistException extends RuntimeException {
    public TicketTypeHasExistException(String message) {
        super(message);
    }
}
