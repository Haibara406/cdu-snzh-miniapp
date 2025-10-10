package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 账户不存在异常
 * @since 2025/9/20 23:28
 */
public class AccountNotFoundException extends BaseException {
    
    public AccountNotFoundException(String message) {
        super(ErrorCodeEnum.ACCOUNT_NOT_FOUND.getCode(), message);
    }

    public AccountNotFoundException() {
        super(ErrorCodeEnum.ACCOUNT_NOT_FOUND.getCode(), ErrorCodeEnum.ACCOUNT_NOT_FOUND.getMessage());
    }
}
