package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 账户禁用异常
 * @since 2025/9/20 23:00
 */
public class AccountLockedException extends BaseException {
    
    public AccountLockedException(String message) {
        super(ErrorCodeEnum.ACCOUNT_LOCKED.getCode(), message);
    }

    public AccountLockedException() {
        super(ErrorCodeEnum.ACCOUNT_LOCKED.getCode(), ErrorCodeEnum.ACCOUNT_LOCKED.getMessage());
    }
}
