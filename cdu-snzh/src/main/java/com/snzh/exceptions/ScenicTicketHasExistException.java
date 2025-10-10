package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 景点门票已存在异常
 * @since 2025/9/29 17:38
 */
public class ScenicTicketHasExistException extends BaseException {
    
    public ScenicTicketHasExistException(String message) {
        super(ErrorCodeEnum.SCENIC_TICKET_HAS_EXIST.getCode(), message);
    }

    public ScenicTicketHasExistException() {
        super(ErrorCodeEnum.SCENIC_TICKET_HAS_EXIST.getCode(), ErrorCodeEnum.SCENIC_TICKET_HAS_EXIST.getMessage());
    }
}
