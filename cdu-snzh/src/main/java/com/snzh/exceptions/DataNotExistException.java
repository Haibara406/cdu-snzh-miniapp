package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 数据不存在异常
 * @since 2025/9/20 23:25
 */
public class DataNotExistException extends BaseException {
    
    public DataNotExistException(String message) {
        super(ErrorCodeEnum.DATA_NOT_EXIST.getCode(), message);
    }

    public DataNotExistException() {
        super(ErrorCodeEnum.DATA_NOT_EXIST.getCode(), ErrorCodeEnum.DATA_NOT_EXIST.getMessage());
    }
}
