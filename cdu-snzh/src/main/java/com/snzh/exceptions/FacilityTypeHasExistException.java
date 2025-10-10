package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 基础设施类型已存在异常
 * @since 2025/9/21 16:59
 */
public class FacilityTypeHasExistException extends BaseException {
    
    public FacilityTypeHasExistException(String message) {
        super(ErrorCodeEnum.FACILITY_TYPE_HAS_EXIST.getCode(), message);
    }

    public FacilityTypeHasExistException() {
        super(ErrorCodeEnum.FACILITY_TYPE_HAS_EXIST.getCode(), ErrorCodeEnum.FACILITY_TYPE_HAS_EXIST.getMessage());
    }
}
