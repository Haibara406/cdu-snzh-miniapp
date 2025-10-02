package com.snzh.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author haibara
 * @description 微信订阅消息模板枚举
 * @since 2025/10/2
 */
@Getter
@AllArgsConstructor
public enum NotificationTemplateEnum {

    /**
     * 订单支付成功通知
     * 参数：订单号、支付金额、支付时间、温馨提示
     */
    ORDER_PAYMENT_SUCCESS("ORDER_PAYMENT_SUCCESS", "订单支付成功通知"),

    /**
     * 订单退款成功通知
     * 参数：订单号、退款金额、退款时间、温馨提示
     */
    ORDER_REFUND_SUCCESS("ORDER_REFUND_SUCCESS", "订单退款成功通知"),

    /**
     * 游玩日期提醒
     * 参数：景点名称、游玩日期、订单号、温馨提示
     */
    VISIT_DATE_REMINDER("VISIT_DATE_REMINDER", "游玩日期提醒"),

    /**
     * 订单即将过期提醒
     * 参数：订单号、过期时间、订单金额、温馨提示
     */
    ORDER_EXPIRE_REMINDER("ORDER_EXPIRE_REMINDER", "订单即将过期提醒"),

    /**
     * 系统公告通知
     * 参数：公告标题、公告内容、发布时间
     */
    SYSTEM_ANNOUNCEMENT("SYSTEM_ANNOUNCEMENT", "系统公告通知");

    /**
     * 模板标识
     */
    private final String templateKey;

    /**
     * 模板描述
     */
    private final String description;
}

