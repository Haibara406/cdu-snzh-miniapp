package com.snzh.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author haibara
 * @description 订单状态枚举
 * @since 2025/10/2
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    /**
     * 订单状态
     */
    PENDING(0, "待支付"),
    PAID(1, "已支付"),
    CANCELLED(2, "已取消"),
    REFUNDED(3, "已退款"),
    COMPLETED(4, "已完成");

    private final Integer code;

    private final String msg;

    public static String getMsg(Integer code) {
        for (OrderStatusEnum status : OrderStatusEnum.values()) {
            if (status.code.intValue() == code.intValue()) {
                return status.msg;
            }
        }
        return "";
    }

    public static OrderStatusEnum getByCode(Integer code) {
        for (OrderStatusEnum status : OrderStatusEnum.values()) {
            if (status.code.intValue() == code.intValue()) {
                return status;
            }
        }
        return null;
    }
}

