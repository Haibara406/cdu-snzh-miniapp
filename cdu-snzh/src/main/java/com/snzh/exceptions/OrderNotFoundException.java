package com.snzh.exceptions;

/**
 * @author haibara
 * @description 订单不存在异常
 * @since 2025/10/2
 */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}

