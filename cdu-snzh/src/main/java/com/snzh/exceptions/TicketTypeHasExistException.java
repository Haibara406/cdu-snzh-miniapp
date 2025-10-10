package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 票种已存在异常
 * @since 2025/9/28 18:05
 */
public class TicketTypeHasExistException extends BaseException {
    
    public TicketTypeHasExistException(String message) {
        super(ErrorCodeEnum.TICKET_TYPE_HAS_EXIST.getCode(), message);
    }

    public TicketTypeHasExistException() {
        super(ErrorCodeEnum.TICKET_TYPE_HAS_EXIST.getCode(), ErrorCodeEnum.TICKET_TYPE_HAS_EXIST.getMessage());
    }
}
