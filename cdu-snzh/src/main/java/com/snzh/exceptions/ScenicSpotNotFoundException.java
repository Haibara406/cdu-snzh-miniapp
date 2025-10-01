package com.snzh.exceptions;

/**
 * @author haibara
 * @description 景点不存在异常
 * @since 2025/9/29 17:49
 */
public class ScenicSpotNotFoundException extends RuntimeException {
    public ScenicSpotNotFoundException(String message) {
        super(message);
    }
}
