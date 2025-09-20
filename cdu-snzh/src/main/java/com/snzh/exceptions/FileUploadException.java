package com.snzh.exceptions;

/**
 * @author haibara
 * @description 文件上传异常
 * @since 2025/7/27 15:49
 */
public class FileUploadException extends RuntimeException {
    public FileUploadException(String message) {
        super(message);
    }
}
