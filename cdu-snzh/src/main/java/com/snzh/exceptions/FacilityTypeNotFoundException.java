package com.snzh.exceptions;

/**
 * @author haibara
 * @description 基础设施不存在异常
 * @since 2025/9/21 15:42
 */
public class FacilityTypeNotFoundException extends RuntimeException {
    public FacilityTypeNotFoundException(String message) {
        super(message);
    }
}
