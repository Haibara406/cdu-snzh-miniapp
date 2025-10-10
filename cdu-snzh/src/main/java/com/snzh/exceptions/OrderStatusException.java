package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 订单状态异常
 * @since 2025/10/2
 */
public class OrderStatusException extends BaseException {
    
    public OrderStatusException(String message) {
        super(ErrorCodeEnum.ORDER_STATUS_ERROR.getCode(), message);
    }

    public OrderStatusException() {
        super(ErrorCodeEnum.ORDER_STATUS_ERROR.getCode(), ErrorCodeEnum.ORDER_STATUS_ERROR.getMessage());
    }
}
