package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 获取手机号异常
 * @since 2025/9/20 23:46
 */
public class GetPhoneNumberException extends BaseException {
    
    public GetPhoneNumberException(String message) {
        super(ErrorCodeEnum.GET_PHONE_NUMBER_FAILED.getCode(), message);
    }

    public GetPhoneNumberException() {
        super(ErrorCodeEnum.GET_PHONE_NUMBER_FAILED.getCode(), ErrorCodeEnum.GET_PHONE_NUMBER_FAILED.getMessage());
    }
}
