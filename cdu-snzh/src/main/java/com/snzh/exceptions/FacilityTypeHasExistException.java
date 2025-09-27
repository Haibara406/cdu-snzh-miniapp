package com.snzh.exceptions;

/**
 * @author haibara
 * @description 基础设施类型已存在异常
 * @since 2025/9/21 16:59
 */
public class FacilityTypeHasExistException extends RuntimeException {
    public FacilityTypeHasExistException(String message) {
        super(message);
    }
}
