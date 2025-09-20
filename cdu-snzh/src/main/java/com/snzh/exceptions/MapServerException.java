package com.snzh.exceptions;

/**
 * @author haibara
 * @description 地图服务异常
 * @since 2025/9/20 14:14
 */
public class MapServerException extends RuntimeException {
    public MapServerException(String message) {
        super(message);
    }
}
