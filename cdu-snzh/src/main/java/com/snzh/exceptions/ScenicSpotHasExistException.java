package com.snzh.exceptions;

/**
 * @author haibara
 * @description 经典已存在异常
 * @since 2025/10/1 15:05
 */
public class ScenicSpotHasExistException extends RuntimeException {
    public ScenicSpotHasExistException(String message) {
        super(message);
    }
}
