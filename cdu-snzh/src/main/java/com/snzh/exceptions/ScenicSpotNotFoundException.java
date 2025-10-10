package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 景点不存在异常
 * @since 2025/9/29 17:49
 */
public class ScenicSpotNotFoundException extends BaseException {
    
    public ScenicSpotNotFoundException(String message) {
        super(ErrorCodeEnum.SCENIC_SPOT_NOT_FOUND.getCode(), message);
    }

    public ScenicSpotNotFoundException() {
        super(ErrorCodeEnum.SCENIC_SPOT_NOT_FOUND.getCode(), ErrorCodeEnum.SCENIC_SPOT_NOT_FOUND.getMessage());
    }
}
