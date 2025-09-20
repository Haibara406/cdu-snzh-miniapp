package com.snzh.redis;

import com.snzh.enums.RedisKeyManage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @author haibara
 * @description redis key包装
 * @since 2025/9/1 18:37
 */

@Getter
@AllArgsConstructor
public class RedisKeyBuild {

    /**
     * 业务实际key
     */
    private final String realKey;


    /**
     * 构建真实的key
     * @param redisKeyManage key的枚举
     * @param args 占位符的值
     * */
    public static RedisKeyBuild createKey(RedisKeyManage redisKeyManage, Object...args){
        String redisRelKey = String.format(redisKeyManage.getKey(), args);
        return new RedisKeyBuild(redisRelKey);
    }

    public static String getRedisKey(RedisKeyManage redisKeyManage){
        return redisKeyManage.getKey();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        RedisKeyBuild that = (RedisKeyBuild) o;
        return this.realKey.equals(that.realKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.realKey);
    }


}
