package com.snzh.handler;

import com.snzh.domain.ResponseResult;
import com.snzh.enums.RespEnum;
import com.snzh.exceptions.BaseException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author haibara
 * @description 全局异常处理器
 * @since 2025/7/27 15:59
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionControllerHandler {

    /**
     * 处理所有业务异常（BaseException及其子类）
     */
    @ExceptionHandler(BaseException.class)
    public ResponseResult<Object> handleBusinessException(BaseException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ResponseResult.failure(e.getCode(), e.getMessage(), e.getData());
    }

    /**
     * 处理参数校验异常（Bean Validation - 方法参数级别）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseResult<Void> handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String message = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验异常: {}", message);
        return ResponseResult.failure(RespEnum.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理参数校验异常（Bean Validation - 对象级别）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseResult<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验异常: {}", message);
        return ResponseResult.failure(RespEnum.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseResult<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String message = String.format("缺少必需参数: %s", e.getParameterName());
        log.warn("缺少请求参数: {}", message);
        return ResponseResult.failure(RespEnum.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseResult<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = String.format("参数类型错误: %s", e.getName());
        log.warn("参数类型不匹配: {}", message);
        return ResponseResult.failure(RespEnum.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理请求体不可读异常（JSON格式错误等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseResult<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return ResponseResult.failure(RespEnum.PARAM_ERROR.getCode(), "请求体格式错误");
    }

    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseResult<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        String message = String.format("不支持的请求方法: %s", e.getMethod());
        log.warn("请求方法不支持: {}", message);
        return ResponseResult.failure(RespEnum.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseResult<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: ", e);
        return ResponseResult.failure(RespEnum.OTHER_ERROR.getCode(), "系统繁忙，请稍后重试");
    }

    /**
     * 处理所有未捕获的异常（兜底）
     */
    @ExceptionHandler(Exception.class)
    public ResponseResult<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return ResponseResult.failure(RespEnum.OTHER_ERROR.getCode(), "系统异常，请联系管理员");
    }
}
