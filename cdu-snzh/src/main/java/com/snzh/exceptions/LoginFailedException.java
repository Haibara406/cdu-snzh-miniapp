package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 登录失败异常
 * @since 2025/9/20 23:17
 */
public class LoginFailedException extends BaseException {
    
    public LoginFailedException(String message) {
        super(ErrorCodeEnum.LOGIN_FAILED.getCode(), message);
    }

    public LoginFailedException() {
        super(ErrorCodeEnum.LOGIN_FAILED.getCode(), ErrorCodeEnum.LOGIN_FAILED.getMessage());
    }
}
