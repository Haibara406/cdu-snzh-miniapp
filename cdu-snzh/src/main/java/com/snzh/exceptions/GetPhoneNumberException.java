package com.snzh.exceptions;

/**
 * @author haibara
 * @description 获取手机号异常
 * @since 2025/9/20 23:46
 */
public class GetPhoneNumberException extends RuntimeException {
    public GetPhoneNumberException(String message) {
        super(message);
    }
}
