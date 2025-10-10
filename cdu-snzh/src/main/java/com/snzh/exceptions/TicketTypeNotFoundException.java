package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 票种不存在异常
 * @since 2025/9/28 18:53
 */
public class TicketTypeNotFoundException extends BaseException {
    
    public TicketTypeNotFoundException(String message) {
        super(ErrorCodeEnum.TICKET_TYPE_NOT_FOUND.getCode(), message);
    }

    public TicketTypeNotFoundException() {
        super(ErrorCodeEnum.TICKET_TYPE_NOT_FOUND.getCode(), ErrorCodeEnum.TICKET_TYPE_NOT_FOUND.getMessage());
    }
}
