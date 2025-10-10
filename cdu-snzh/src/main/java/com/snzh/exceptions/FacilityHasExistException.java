package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 基础设施已经存在异常
 * @since 2025/9/21 22:33
 */
public class FacilityHasExistException extends BaseException {
    
    public FacilityHasExistException(String message) {
        super(ErrorCodeEnum.FACILITY_HAS_EXIST.getCode(), message);
    }

    public FacilityHasExistException() {
        super(ErrorCodeEnum.FACILITY_HAS_EXIST.getCode(), ErrorCodeEnum.FACILITY_HAS_EXIST.getMessage());
    }
}
