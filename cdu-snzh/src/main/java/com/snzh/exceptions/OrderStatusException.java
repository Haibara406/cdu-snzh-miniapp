package com.snzh.exceptions;

/**
 * @author haibara
 * @description 订单状态异常
 * @since 2025/10/2
 */
public class OrderStatusException extends RuntimeException {
    public OrderStatusException(String message) {
        super(message);
    }
}

