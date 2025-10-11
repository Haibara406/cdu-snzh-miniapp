package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 管理员登录失败异常
 * @since 2025/10/11
 */
public class AdminLoginException extends BaseException {
    
    public AdminLoginException(String message) {
        super(ErrorCodeEnum.ADMIN_LOGIN_FAILED.getCode(), message);
    }

    public AdminLoginException() {
        super(ErrorCodeEnum.ADMIN_LOGIN_FAILED.getCode(), ErrorCodeEnum.ADMIN_LOGIN_FAILED.getMessage());
    }
}

