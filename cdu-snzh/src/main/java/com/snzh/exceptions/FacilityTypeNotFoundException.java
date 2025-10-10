package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 基础设施类型不存在异常
 * @since 2025/9/21 15:42
 */
public class FacilityTypeNotFoundException extends BaseException {
    
    public FacilityTypeNotFoundException(String message) {
        super(ErrorCodeEnum.FACILITY_TYPE_NOT_FOUND.getCode(), message);
    }

    public FacilityTypeNotFoundException() {
        super(ErrorCodeEnum.FACILITY_TYPE_NOT_FOUND.getCode(), ErrorCodeEnum.FACILITY_TYPE_NOT_FOUND.getMessage());
    }
}
