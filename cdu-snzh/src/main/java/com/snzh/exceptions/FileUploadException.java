package com.snzh.exceptions;

import com.snzh.enums.ErrorCodeEnum;

/**
 * @author haibara
 * @description 文件上传异常
 * @since 2025/7/27 15:49
 */
public class FileUploadException extends BaseException {
    
    public FileUploadException(String message) {
        super(ErrorCodeEnum.FILE_UPLOAD_ERROR.getCode(), message);
    }

    public FileUploadException() {
        super(ErrorCodeEnum.FILE_UPLOAD_ERROR.getCode(), ErrorCodeEnum.FILE_UPLOAD_ERROR.getMessage());
    }
}
