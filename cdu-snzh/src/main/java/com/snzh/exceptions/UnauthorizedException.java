package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 无权限访问异常
 * @since 2025/10/11
 */
public class UnauthorizedException extends BaseException {
    
    public UnauthorizedException(String message) {
        super(ErrorCodeEnum.UNAUTHORIZED.getCode(), message);
    }

    public UnauthorizedException() {
        super(ErrorCodeEnum.UNAUTHORIZED.getCode(), ErrorCodeEnum.UNAUTHORIZED.getMessage());
    }
}

