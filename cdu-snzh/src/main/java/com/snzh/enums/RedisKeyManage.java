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

    USER_LOGIN("user_login_%s","user_login","value为UserVo类型"),

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
