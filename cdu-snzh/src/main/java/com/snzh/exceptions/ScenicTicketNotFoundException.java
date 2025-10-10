package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 景点门票不存在异常
 * @since 2025/9/29 16:55
 */
public class ScenicTicketNotFoundException extends BaseException {
    
    public ScenicTicketNotFoundException(String message) {
        super(ErrorCodeEnum.SCENIC_TICKET_NOT_FOUND.getCode(), message);
    }

    public ScenicTicketNotFoundException() {
        super(ErrorCodeEnum.SCENIC_TICKET_NOT_FOUND.getCode(), ErrorCodeEnum.SCENIC_TICKET_NOT_FOUND.getMessage());
    }
}
