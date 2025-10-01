package com.snzh.exceptions;

/**
 * @author haibara
 * @description 景点门票已存在
 * @since 2025/9/29 17:38
 */
public class ScenicTicketHasExistException extends RuntimeException {
    public ScenicTicketHasExistException(String message) {
        super(message);
    }
}
