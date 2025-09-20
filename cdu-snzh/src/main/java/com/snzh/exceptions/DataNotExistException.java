package com.snzh.exceptions;

/**
 * @author haibara
 * @description 数据不存在异常
 * @since 2025/9/20 23:25
 */
public class DataNotExistException extends RuntimeException {
    public DataNotExistException(String message) {
        super(message);
    }
}
