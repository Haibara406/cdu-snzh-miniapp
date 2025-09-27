package com.snzh.exceptions;

/**
 * @author haibara
 * @description 基础设施不存在异常
 * @since 2025/9/21 21:00
 */
public class FacilityNotFoundException extends RuntimeException {
    public FacilityNotFoundException(String message) {
        super(message);
    }
}
