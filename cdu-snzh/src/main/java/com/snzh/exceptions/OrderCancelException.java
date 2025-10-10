package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 订单取消异常
 * @since 2025/10/2
 */
public class OrderCancelException extends BaseException {
    
    public OrderCancelException(String message) {
        super(ErrorCodeEnum.ORDER_CANCEL_FAILED.getCode(), message);
    }

    public OrderCancelException() {
        super(ErrorCodeEnum.ORDER_CANCEL_FAILED.getCode(), ErrorCodeEnum.ORDER_CANCEL_FAILED.getMessage());
    }
}
