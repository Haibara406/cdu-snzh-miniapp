package com.snzh.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author haibara
 * @description 状态枚举
 * @since 2025/9/20 17:18
 */
@Getter
@AllArgsConstructor
public enum StatusEnum {

    /**
     * 基础状态
     * */
    RUN(1,"正常"),
    STOP(0,"禁用")
    ;

    private final Integer code;

    private final String msg;

    public static String getMsg(Integer code) {
        for (StatusEnum re : StatusEnum.values()) {
            if (re.code.intValue() == code.intValue()) {
                return re.msg;
            }
        }
        return "";
    }

    public static StatusEnum getRc(Integer code) {
        for (StatusEnum re : StatusEnum.values()) {
            if (re.code.intValue() == code.intValue()) {
                return re;
            }
        }
        return null;
    }
}
