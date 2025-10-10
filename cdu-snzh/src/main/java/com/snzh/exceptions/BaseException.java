package com.snzh.exceptions;

import lombok.Getter;

/**
 * @author haibara
 * @description 基础业务异常类
 * @since 2025/10/10
 */
@Getter
public class BaseException extends RuntimeException {

    /**
     * 错误码（默认500）
     */
    private final Integer code;

    /**
     * 错误详细数据（可选）
     */
    private final Object data;

    public BaseException(String message) {
        super(message);
        this.code = 500;
        this.data = null;
    }

    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
        this.data = null;
    }

    public BaseException(Integer code, String message, Object data) {
        super(message);
        this.code = code;
        this.data = data;
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
        this.data = null;
    }

    public BaseException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.data = null;
    }
}

