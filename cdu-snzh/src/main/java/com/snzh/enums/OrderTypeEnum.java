package com.snzh.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author haibara
 * @description 订单类型枚举
 * @since 2025/10/2
 */
@Getter
@AllArgsConstructor
public enum OrderTypeEnum {

    /**
     * 订单类型
     */
    TICKET(1, "景点门票"),
    ACCOMMODATION(2, "住宿"),
    CATERING(3, "餐饮"),
    CULTURAL_PRODUCT(4, "文创商品");

    private final Integer code;

    private final String msg;

    public static String getMsg(Integer code) {
        for (OrderTypeEnum type : OrderTypeEnum.values()) {
            if (type.code.intValue() == code.intValue()) {
                return type.msg;
            }
        }
        return "";
    }

    public static OrderTypeEnum getByCode(Integer code) {
        for (OrderTypeEnum type : OrderTypeEnum.values()) {
            if (type.code.intValue() == code.intValue()) {
                return type;
            }
        }
        return null;
    }
}

