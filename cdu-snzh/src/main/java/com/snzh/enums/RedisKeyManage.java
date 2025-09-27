package com.snzh.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author haibara
 * @description redis key统一管理枚举
 * @since 2025/9/1 17:27
 */
@Getter
@AllArgsConstructor
public enum RedisKeyManage {

    /**
     * redis 缓存 key管理
     * */

    Key("key","键值测试","value为TestCacheDto类型"),
    Key2("key:%s","键值占位测试","value为TestCacheDto类型"),

    USER_LOGIN("user:login:%s","用户id" ,"value为refreshToken"),
    FACILITY_TYPE_SINGLE("facility:type:%s", "基础设施类型id", "value为FacilityType类型"),
    FACILITY_TYPE_ALL("facility:type:all", "", "value为List<FacilityTypeVO>"),
    FACILITY_FOR_TYPE("facility:%s", "基础设施类型id", "value为List<FacilityVO>类型"),
    FACILITY_DETAILS("facility:details:%s", "基础设施id", "value为Facility类型")
    ;



    /**
     * key值
     * */
    private final String key;

    /**
     * key的说明
     * */
    private final String keyIntroduce;

    /**
     * value的说明
     * */
    private final String valueIntroduce;


    public static RedisKeyManage getRc(String keyCode) {
        for (RedisKeyManage re : RedisKeyManage.values()) {
            if (re.key.equals(keyCode)) {
                return re;
            }
        }
        return null;
    }
}
