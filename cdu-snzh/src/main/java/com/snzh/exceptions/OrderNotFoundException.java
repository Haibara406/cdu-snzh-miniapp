package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 订单不存在异常
 * @since 2025/10/2
 */
public class OrderNotFoundException extends BaseException {
    
    public OrderNotFoundException(String message) {
        super(ErrorCodeEnum.ORDER_NOT_FOUND.getCode(), message);
    }

    public OrderNotFoundException() {
        super(ErrorCodeEnum.ORDER_NOT_FOUND.getCode(), ErrorCodeEnum.ORDER_NOT_FOUND.getMessage());
    }
}
