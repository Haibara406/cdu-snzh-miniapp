package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 基础设施不存在异常
 * @since 2025/9/21 21:00
 */
public class FacilityNotFoundException extends BaseException {
    
    public FacilityNotFoundException(String message) {
        super(ErrorCodeEnum.FACILITY_NOT_FOUND.getCode(), message);
    }

    public FacilityNotFoundException() {
        super(ErrorCodeEnum.FACILITY_NOT_FOUND.getCode(), ErrorCodeEnum.FACILITY_NOT_FOUND.getMessage());
    }
}
