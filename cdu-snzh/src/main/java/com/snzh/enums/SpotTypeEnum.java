package com.snzh.enums;

import lombok.Getter;

/**
 * @author haibara
 * @description 景区/景点类型枚举
 * @since 2025/10/7
 */
@Getter
public enum SpotTypeEnum {
    
    /**
     * 景区（如蜀南竹海）- 可售票的顶级景区
     */
    SCENIC_AREA(0, "景区"),
    
    /**
     * 景点（如忘忧谷、天宝寨）- 景区内的景点，不单独售票
     */
    SCENIC_SPOT(1, "景点");

    private final Integer code;
    private final String desc;

    SpotTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static SpotTypeEnum fromCode(Integer code) {
        if (code == null) {
            return SCENIC_SPOT; // 默认为景点
        }
        for (SpotTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return SCENIC_SPOT;
    }

    /**
     * 判断是否为景区
     */
    public static boolean isScenicArea(Integer code) {
        return SCENIC_AREA.getCode().equals(code);
    }

    /**
     * 判断是否为景点
     */
    public static boolean isScenicSpot(Integer code) {
        return SCENIC_SPOT.getCode().equals(code);
    }
}


