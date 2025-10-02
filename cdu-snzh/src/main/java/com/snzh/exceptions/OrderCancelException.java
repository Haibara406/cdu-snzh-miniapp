package com.snzh.exceptions;

/**
 * @author haibara
 * @description 订单取消异常
 * @since 2025/10/2
 */
public class OrderCancelException extends RuntimeException {
    public OrderCancelException(String message) {
        super(message);
    }
}

