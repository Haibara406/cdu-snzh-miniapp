package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 登录尝试次数过多异常
 * @since 2025/10/11
 */
public class TooManyLoginAttemptsException extends BaseException {
    
    public TooManyLoginAttemptsException(String message) {
        super(ErrorCodeEnum.TOO_MANY_LOGIN_ATTEMPTS.getCode(), message);
    }

    public TooManyLoginAttemptsException(Integer code, String message) {
        super(code, message);
    }
}

