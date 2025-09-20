package com.snzh.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author haibara
 * @description 状态码枚举
 * @since 2025/7/27 12:19
 */
@Getter
@AllArgsConstructor
@Schema(description = "响应状态码枚举")
public enum RespEnum {

    /**
     * 请求成功
     */
    @Schema(description = "请求成功")
    SUCCESS(200, "success"),
    /**
     * 请求失败
     */
    @Schema(description = "请求失败")
    FAILURE(500, "failure"),
    /**
     * 用户名或密码错误
     */
    @Schema(description = "用户名或密码错误")
    USERNAME_OR_PASSWORD_ERROR(1001, "用户名或密码错误"),
    /**
     * 未登录提示
     */
    @Schema(description = "未登录提示")
    NOT_LOGIN(1002, "请先登录"),
    /**
     * 用户名或邮箱已存在
     */
    @Schema(description = "用户名或邮箱已存在")
    USERNAME_OR_EMAIL_EXIST(1006, "用户名或邮箱已存在"),
    /**
     * 参数错误提示
     */
    @Schema(description = "参数错误提示")
    PARAM_ERROR(1007, "参数错误"),
    /**
     * 其他故障
     */
    @Schema(description = "其他故障")
    OTHER_ERROR(1008, "其他故障"),
    /**
     * 文件上传错误
     */
    @Schema(description = "文件上传错误")
    FILE_UPLOAD_ERROR(1011, "文件上传错误"),
    /**
     * 账号被封禁
     */
    @Schema(description = "账号被封禁")
    BLACK_LIST_ERROR(1012, "账号被封禁");



    /**
     * code
     */
    private final Integer code;

    /**
     * Msg
     */
    private final String msg;

}
