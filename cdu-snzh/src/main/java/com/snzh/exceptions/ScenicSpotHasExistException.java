package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 景点已存在异常
 * @since 2025/10/1 15:05
 */
public class ScenicSpotHasExistException extends BaseException {
    
    public ScenicSpotHasExistException(String message) {
        super(ErrorCodeEnum.SCENIC_SPOT_HAS_EXIST.getCode(), message);
    }

    public ScenicSpotHasExistException() {
        super(ErrorCodeEnum.SCENIC_SPOT_HAS_EXIST.getCode(), ErrorCodeEnum.SCENIC_SPOT_HAS_EXIST.getMessage());
    }
}
