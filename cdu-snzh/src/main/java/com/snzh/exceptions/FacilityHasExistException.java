package com.snzh.exceptions;

/**
 * @author haibara
 * @description 基础设施已经存在异常
 * @since 2025/9/21 22:33
 */
public class FacilityHasExistException extends RuntimeException {
    public FacilityHasExistException(String message) {
        super(message);
    }
}
