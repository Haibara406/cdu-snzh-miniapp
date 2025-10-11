package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 管理员用户异常
 * @since 2025/10/11
 */
public class AdminUserException extends BaseException {
    
    public AdminUserException(String message) {
        super(ErrorCodeEnum.ADMIN_USER_ERROR.getCode(), message);
    }

    public AdminUserException(Integer code, String message) {
        super(code, message);
    }
}

