package com.snzh.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author haibara
 * @description 订单项类型枚举
 * @since 2025/10/2
 */
@Getter
@AllArgsConstructor
public enum ItemTypeEnum {

    /**
     * 订单项类型
     */
    TICKET(1, "门票"),
    ACCOMMODATION(2, "住宿"),
    CATERING(3, "餐饮"),
    GOODS(4, "商品");

    private final Integer code;

    private final String msg;

    public static String getMsg(Integer code) {
        for (ItemTypeEnum type : ItemTypeEnum.values()) {
            if (type.code.intValue() == code.intValue()) {
                return type.msg;
            }
        }
        return "";
    }

    public static ItemTypeEnum getByCode(Integer code) {
        for (ItemTypeEnum type : ItemTypeEnum.values()) {
            if (type.code.intValue() == code.intValue()) {
                return type;
            }
        }
        return null;
    }
}

