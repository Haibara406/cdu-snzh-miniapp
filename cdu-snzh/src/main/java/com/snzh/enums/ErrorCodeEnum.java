package com.snzh.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author haibara
 * @description 业务错误码枚举
 * @since 2025/10/10
 */
@Getter
@AllArgsConstructor
public enum ErrorCodeEnum {

    // ==================== 通用错误 2000-2099 ====================
    DATA_NOT_EXIST(2001, "数据不存在"),
    PARAM_ERROR(2002, "参数错误"),

    // ==================== 用户相关 2100-2149 ====================
    ACCOUNT_NOT_FOUND(2100, "账号不存在"),
    ACCOUNT_LOCKED(2101, "账号已被锁定"),
    LOGIN_FAILED(2102, "登录失败"),
    GET_PHONE_NUMBER_FAILED(2103, "获取手机号失败"),
    UNAUTHORIZED(2104, "无权限访问"),
    ADMIN_LOGIN_FAILED(2105, "管理员登录失败"),

    // ==================== 管理员相关 2150-2199 ====================
    ADMIN_USER_ERROR(2150, "管理员操作异常"),
    ADMIN_NOT_FOUND(2151, "管理员不存在"),
    ADMIN_USERNAME_EXISTS(2152, "管理员用户名已存在"),
    CANNOT_DELETE_SUPER_ADMIN(2153, "不能删除超级管理员"),
    CANNOT_DISABLE_SUPER_ADMIN(2154, "不能禁用超级管理员"),
    CANNOT_DELETE_SELF(2155, "不能删除自己的账号"),
    CANNOT_DISABLE_SELF(2156, "不能禁用自己的账号"),

    // ==================== 基础设施相关 2200-2299 ====================
    FACILITY_NOT_FOUND(2200, "基础设施不存在"),
    FACILITY_HAS_EXIST(2201, "基础设施已存在"),
    FACILITY_TYPE_NOT_FOUND(2202, "基础设施类型不存在"),
    FACILITY_TYPE_HAS_EXIST(2203, "基础设施类型已存在"),

    // ==================== 景点相关 2300-2399 ====================
    SCENIC_SPOT_NOT_FOUND(2300, "景点不存在"),
    SCENIC_SPOT_HAS_EXIST(2301, "景点已存在"),
    SCENIC_TICKET_NOT_FOUND(2302, "景点门票不存在"),
    SCENIC_TICKET_HAS_EXIST(2303, "景点门票已存在"),
    TICKET_TYPE_NOT_FOUND(2304, "票种不存在"),
    TICKET_TYPE_HAS_EXIST(2305, "票种已存在"),

    // ==================== 订单相关 2400-2499 ====================
    ORDER_NOT_FOUND(2400, "订单不存在"),
    ORDER_STATUS_ERROR(2401, "订单状态错误"),
    ORDER_CANCEL_FAILED(2402, "订单取消失败"),

    // ==================== 文件相关 2500-2599 ====================
    FILE_UPLOAD_ERROR(2500, "文件上传失败"),

    // ==================== 第三方服务相关 2600-2699 ====================
    MAP_SERVER_ERROR(2600, "地图服务异常");

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;
}

