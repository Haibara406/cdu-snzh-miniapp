package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 地图服务异常
 * @since 2025/9/20 14:14
 */
public class MapServerException extends BaseException {
    
    public MapServerException(String message) {
        super(ErrorCodeEnum.MAP_SERVER_ERROR.getCode(), message);
    }

    public MapServerException() {
        super(ErrorCodeEnum.MAP_SERVER_ERROR.getCode(), ErrorCodeEnum.MAP_SERVER_ERROR.getMessage());
    }
}
