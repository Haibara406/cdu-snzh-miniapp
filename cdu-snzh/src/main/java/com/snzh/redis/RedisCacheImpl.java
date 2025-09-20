package com.snzh.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.snzh.utils.CacheUtil;
import com.snzh.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author haibara
 * @description redis方法实现
 * @since 2025/9/1 18:15
 */
@RequiredArgsConstructor
@Service
public class RedisCacheImpl implements RedisCache {

    private final StringRedisTemplate redisTemplate;

    /**
     * 获取字符串对象
     *
     * @param redisKeyBuild RedisKeyBuild
     * @param clazz         类对象
     * @return T 普通对象
     */
    @Override
    public <T> T get(RedisKeyBuild redisKeyBuild, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String realKey = redisKeyBuild.getRealKey();
        String cachedValue = this.redisTemplate.opsForValue().get(realKey);
        if(String.class.isAssignableFrom(clazz)){
            return (T)cachedValue;
        }
        return getComplex(cachedValue, clazz);
    }

    /**
     * 获取字符串对象(如果缓存中不存在，则执行给定的supplier接口)
     *
     * @param redisKeyBuild RedisKeyBuild
     * @param clazz         类对象
     * @param supplier      缓存为空时，执行的逻辑
     * @param ttl           过期时间
     * @param timeUnit      时间单位
     * @return T 普通对象
     */
    @Override
    public <T> T get(RedisKeyBuild redisKeyBuild, Class<T> clazz, Supplier<T> supplier, long ttl, TimeUnit timeUnit) {
        T t = get(redisKeyBuild, clazz);
        if(CacheUtil.isEmpty(t)){
            t = supplier.get();
            if(CacheUtil.isEmpty(t)){
                return null;
            }
            set(redisKeyBuild,t,ttl,timeUnit);
        }
        return t;
    }

    /**
     * 返回 key 中字符串值的子字符
     *
     * @param redisKeyBuild 缓存key
     * @param start         开始
     * @param end           结束
     * @return 结果
     */
    @Override
    public String getRange(RedisKeyBuild redisKeyBuild, long start, long end) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String realKey = redisKeyBuild.getRealKey();
        return this.redisTemplate.opsForValue().get(realKey,start,end);
    }

    /**
     * 获取字符串对象, 并且字符串中是集合内容
     *
     * @param redisKeyBuild 缓存key
     * @param clazz         类型
     * @return List<T>
     */
    @Override
    public <T> List<T> getValueIsList(RedisKeyBuild redisKeyBuild, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String realKey = redisKeyBuild.getRealKey();
        String s = this.redisTemplate.opsForValue().get(realKey);
        if(CacheUtil.isEmpty(s)){
            return new ArrayList<>();
        }
        return JSON.parseArray(s,clazz);
    }

    /**
     * 获取字符串对象, 并且字符串中是集合内容(如果缓存中不存在，则执行给定的supplier接口)
     *
     * @param redisKeyBuild 缓存key
     * @param clazz         类型
     * @param supplier      缓存为空时，执行的逻辑
     * @param ttl           过期时间
     * @param timeUnit      时间单位
     * @return List<T>
     */
    @Override
    public <T> List<T> getValueIsList(RedisKeyBuild redisKeyBuild, Class<T> clazz, Supplier<List<T>> supplier, long ttl, TimeUnit timeUnit) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String realKey = redisKeyBuild.getRealKey();
        String s = this.redisTemplate.opsForValue().get(realKey);
        List<T> tList = null;
        if(CacheUtil.isEmpty(s)){
            tList = supplier.get();
            if(CacheUtil.isEmpty(tList)){
                return null;
            }
            set(redisKeyBuild,tList,ttl,timeUnit);
        }
        return tList;
    }

    /**
     * 通过多个key批量获取多个value
     *
     * @param keyList key集合
     * @return List<String>
     */
    @Override
    public List<String> getKeys(List<RedisKeyBuild> keyList) {
        CacheUtil.checkNotEmpty(keyList);
        List<String> batchKey = CacheUtil.getBatchKey(keyList);
        List<String> list = this.redisTemplate.opsForValue().multiGet(batchKey);

        return CacheUtil.optimizeRedisList(list);
    }

    /**
     * 判断key是否存在
     *
     * @param redisKeyBuild redisKeyBuild
     * @return 是否存在 可能为空
     */
    @Override
    public Boolean hasKey(RedisKeyBuild redisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String realKey = redisKeyBuild.getRealKey();
        return this.redisTemplate.hasKey(realKey);
    }

    /**
     * 删除key
     *
     * @param redisKeyBuild 缓存key
     * @return
     */
    @Override
    public void del(RedisKeyBuild redisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        this.redisTemplate.delete(redisKeyBuild.getRealKey());
    }

    /**
     * 批量删除key
     *
     * @param keys key集合
     */
    @Override
    public void del(Collection<RedisKeyBuild> keys) {
        CacheUtil.checkNotEmpty(keys);
        List<String> batchKey = CacheUtil.getBatchKey(keys);
        this.redisTemplate.delete(batchKey);
    }

    /**
     * 设置key过期时间
     *
     * @param redisKeyBuild RedisKeyBuild
     * @param ttl           过期时间
     * @param timeUnit      时间单位
     * @return 是否成功
     */
    @Override
    public Boolean expire(RedisKeyBuild redisKeyBuild, long ttl, TimeUnit timeUnit) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String realKey = redisKeyBuild.getRealKey();
        return this.redisTemplate.expire(realKey, ttl, timeUnit);
    }

    /**
     * 获取key超时时间
     *
     * @param redisKeyBuild redisKeyBuild
     * @return 超时时间
     */
    @Override
    public Long getExpire(RedisKeyBuild redisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String realKey = redisKeyBuild.getRealKey();
        return this.redisTemplate.getExpire(realKey);
    }

    /**
     * 获取key超时时间
     *
     * @param redisKeyBuild redisKeyBuild
     * @param timeUnit      时间单位
     * @return 超时时间
     */
    @Override
    public Long getExpire(RedisKeyBuild redisKeyBuild, TimeUnit timeUnit) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String realKey = redisKeyBuild.getRealKey();
        return this.redisTemplate.getExpire(realKey, timeUnit);
    }

    /**
     * 查找匹配的key
     *
     * @param pattern 缓存key
     * @return keys
     */
    @Override
    public Set<String> keys(String pattern) {
        return this.redisTemplate.keys(pattern);
    }

    /**
     * 将当前数据库的 key 移动到给定的数据库 db 当中
     *
     * @param redisKeyBuild 缓存key
     * @param dbIndex
     * @return
     */
    @Override
    public Boolean move(RedisKeyBuild redisKeyBuild, int dbIndex) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String realKey = redisKeyBuild.getRealKey();
        return this.redisTemplate.move(realKey, dbIndex);
    }

    /**
     * 移除 key 的过期时间，key 将持久保持
     *
     * @param redisKeyBuild 缓存key
     * @return
     */
    @Override
    public Boolean persist(RedisKeyBuild redisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String realKey = redisKeyBuild.getRealKey();
        return this.redisTemplate.persist(realKey);
    }

    /**
     * 从当前数据库中随机返回一个 key
     *
     * @return
     */
    @Override
    public String randomKey() {
        return this.redisTemplate.randomKey();
    }

    /**
     * 修改 key 的名称
     *
     * @param oldKey 缓存key
     * @param newKey 缓存key
     */
    @Override
    public void rename(RedisKeyBuild oldKey, RedisKeyBuild newKey) {
        CacheUtil.checkNotBlank(oldKey);
        CacheUtil.checkNotBlank(newKey);
        this.redisTemplate.rename(oldKey.getRealKey(),newKey.getRealKey());
    }

    /**
     * 仅当 newKey 不存在时，将 oldKey 改名为 newKey
     *
     * @param oldKey 缓存key
     * @param newKey 缓存key
     * @return
     */
    @Override
    public Boolean renameIfAbsent(RedisKeyBuild oldKey, RedisKeyBuild newKey) {
        CacheUtil.checkNotBlank(oldKey);
        CacheUtil.checkNotBlank(newKey);
        return this.redisTemplate.renameIfAbsent(oldKey.getRealKey(), newKey.getRealKey());
    }

    /**
     * 返回 key 所储存的值的类型
     *
     * @param redisKeyBuild 缓存key
     * @return
     */
    @Override
    public DataType type(RedisKeyBuild redisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String realKey = redisKeyBuild.getRealKey();
        return this.redisTemplate.type(realKey);
    }

    /**
     * 设置缓存
     *
     * @param redisKeyBuild 缓存key
     * @param object        缓存对象
     */
    @Override
    public void set(RedisKeyBuild redisKeyBuild, Object object) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String realKey = redisKeyBuild.getRealKey();
        String s = object instanceof String ? (String) object : JSON.toJSONString(object);
        this.redisTemplate.opsForValue().set(realKey,s);
    }

    /**
     * 设置缓存
     *
     * @param redisKeyBuild 缓存key
     * @param object        缓存对象
     * @param ttl           过期时间
     */
    @Override
    public void set(RedisKeyBuild redisKeyBuild, Object object, long ttl) {
        this.set(redisKeyBuild,object,ttl,CacheUtil.DEFAULT_TIME_UNIT);
    }

    /**
     * 设置缓存
     *
     * @param redisKeyBuild 缓存key
     * @param object        缓存对象
     * @param ttl           过期时间
     * @param timeUnit      时间单位
     */
    @Override
    public void set(RedisKeyBuild redisKeyBuild, Object object, long ttl, TimeUnit timeUnit) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String realKey = redisKeyBuild.getRealKey();
        String s = object instanceof String ? (String) object : JSON.toJSONString(object);
        this.redisTemplate.opsForValue().set(realKey,s,ttl,timeUnit);
    }

    /**
     * 只有在 key 不存在时设置 key 的值
     *
     * @param redisKeyBuild 缓存key
     * @param object        对象
     * @return 之前已经存在返回false, 不存在返回true
     */
    @Override
    public boolean setIfAbsent(RedisKeyBuild redisKeyBuild, Object object) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String realKey = redisKeyBuild.getRealKey();
        String s = object instanceof String ? (String) object : JSON.toJSONString(object);
        return this.redisTemplate.opsForValue().setIfAbsent(realKey, s);
    }

    /**
     * 获取字符串的长度
     *
     * @param redisKeyBuild 缓存key
     * @return 长度
     */
    @Override
    public Long size(RedisKeyBuild redisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        return this.redisTemplate.opsForValue().size(key);
    }

    /**
     * 批量添加
     *
     * @param map 对象
     */
    @Override
    public void multiSet(Map<RedisKeyBuild, ?> map) {
        CacheUtil.checkNotEmpty(map);
        Map<String, String> mapForSave = new HashMap<>(map.size());
        map.forEach(
                (key,value) -> {
                    String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
                    mapForSave.put(key.getRealKey(), jsonValue);
                }
        );
        this.redisTemplate.opsForValue().multiSet(mapForSave);
    }

    /**
     * 同时设置一个或多个 key-value 对，当且仅当所有给定 key 都不存在
     *
     * @param map 对象
     * @return 之前已经存在返回false, 不存在返回true
     */
    @Override
    public boolean multiSetIfAbsent(Map<RedisKeyBuild, ?> map) {
        CacheUtil.checkNotEmpty(map);
        Map<String, String> mapForSave = new HashMap<>(map.size());
        map.forEach(
                (key,value) -> {
                    String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
                    mapForSave.put(key.getRealKey(), jsonValue);
                }
        );
        return this.redisTemplate.opsForValue().multiSetIfAbsent(mapForSave);
    }

    /**
     * 增加(自增长), 负数则为自减
     *
     * @param redisKeyBuild 缓存key
     * @param increment     步长
     * @return
     */
    @Override
    public Long incrBy(RedisKeyBuild redisKeyBuild, long increment) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String realKey = redisKeyBuild.getRealKey();
        return this.redisTemplate.opsForValue().increment(realKey, increment);
    }

    /**
     * double类型增加(自增长), 负数则为自减
     *
     * @param redisKeyBuild 缓存key
     * @param increment     步长
     * @return
     */
    @Override
    public Double incrByDouble(RedisKeyBuild redisKeyBuild, double increment) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        return this.redisTemplate.opsForValue().increment(key, increment);
    }

    /**
     * 追加到末尾
     *
     * @param redisKeyBuild 缓存key
     * @param value         值
     * @return
     */
    @Override
    public Integer append(RedisKeyBuild redisKeyBuild, String value) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        return this.redisTemplate.opsForValue().append(key, value);
    }

    /**
     * 放置一个键值对
     *
     * @param redisKeyBuild hash键
     * @param hashKey       hash key
     * @param value         hash value
     */
    @Override
    public void putHash(RedisKeyBuild redisKeyBuild, String hashKey, Object value) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
        this.redisTemplate.opsForHash().put(key, hashKey, jsonValue);
    }

    /**
     * 放置一个键值对 并设置过期时间
     *
     * @param redisKeyBuild hash键
     * @param hashKey       hash key
     * @param value         hash value
     * @param ttl           过期时间
     */
    @Override
    public void putHash(RedisKeyBuild redisKeyBuild, String hashKey, Object value, long ttl) {
        putHash(redisKeyBuild, hashKey, value, ttl, CacheUtil.DEFAULT_TIME_UNIT);
    }

    /**
     * 放置一个键值对 并设置过期时间
     *
     * @param redisKeyBuild hash键
     * @param hashKey       hash key
     * @param value         hash value
     * @param ttl           过期时间
     * @param timeUnit      时间单位
     */
    @Override
    public void putHash(RedisKeyBuild redisKeyBuild, String hashKey, Object value, long ttl, TimeUnit timeUnit) {
        putHash(redisKeyBuild, hashKey, value);
        // 设置过期时间
        expire(redisKeyBuild, ttl, timeUnit);
    }

    /**
     * 放入map中所有键值对
     *
     * @param redisKeyBuild key
     * @param map           hash
     */
    @Override
    public void putHash(RedisKeyBuild redisKeyBuild, Map<String, ?> map) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        Map<String, String> mapForSave = new HashMap<>(map.size());
        map.forEach((hashKey, val) -> {
            String jsonValue = val instanceof String ? (String) val : JSON.toJSONString(val);
            mapForSave.put(hashKey, jsonValue);
        });
        this.redisTemplate.opsForHash().putAll(key, mapForSave);
    }

    /**
     * 放入map中所有键值对 并设置过期时间
     *
     * @param redisKeyBuild key
     * @param map           hash
     * @param ttl           过期时间
     */
    @Override
    public void putHash(RedisKeyBuild redisKeyBuild, Map<String, ?> map, long ttl) {
        putHash(redisKeyBuild, map, ttl, CacheUtil.DEFAULT_TIME_UNIT);
    }

    /**
     * 放入 Map 中所有键值对 并设置过期时间和时间单位
     *
     * @param redisKeyBuild key
     * @param map           hash
     * @param ttl           过期时间
     * @param timeUnit      时间单位
     */
    @Override
    public void putHash(RedisKeyBuild redisKeyBuild, Map<String, ?> map, long ttl, TimeUnit timeUnit) {
        putHash(redisKeyBuild, map);
        expire(redisKeyBuild, ttl, timeUnit);
    }

    /**
     * 仅当hashKey不存在时才设置
     *
     * @param redisKeyBuild 缓存key
     * @param hashKey       hash中key
     * @param value         对象
     * @return
     */
    @Override
    public Boolean putHashIfAbsent(RedisKeyBuild redisKeyBuild, String hashKey, Object value) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotBlank(hashKey);
        String key = redisKeyBuild.getRealKey();
        String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
        return this.redisTemplate.opsForHash().putIfAbsent(key, hashKey, jsonValue);
    }

    /**
     * 从 Hash 中获取普通对象
     *
     * @param redisKeyBuild key
     * @param hashKey       hash key
     * @param clazz         类对象
     * @return 普通对象
     */
    @Override
    public <T> T getForHash(RedisKeyBuild redisKeyBuild, String hashKey, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotBlank(hashKey);
        String key = redisKeyBuild.getRealKey();
        Object o = this.redisTemplate.opsForHash().get(key, hashKey);
        // 如果取String类型 则直接取出返回
        if (String.class.isAssignableFrom(clazz)) {
            return (T) o;
        }
        return getComplex(o, clazz);
    }

    /**
     * Hash的value是字符串集合，进行提取
     *
     * @param redisKeyBuild key
     * @param hashKey       hash key
     * @param clazz         类对象
     * @return 普通对象
     */
    @Override
    public <T> List<T> getValueIsListForHash(RedisKeyBuild redisKeyBuild, String hashKey, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotBlank(hashKey);
        String key = redisKeyBuild.getRealKey();
        Object o = this.redisTemplate.opsForHash().get(key,hashKey);
        if (o == null) {
            return new ArrayList<>();
        }
        List<T> list = new ArrayList<>();
        if (o instanceof String) {
            list = JSON.parseArray((String)o, clazz);
        }
        return list;
    }

    /**
     * 从 {@code key} 处获取给定 {@code hashKeys} 的值
     *
     * @param redisKeyBuild key
     * @param hashKeys      hashKeys
     * @param clazz         类对象
     * @return
     */
    @Override
    public <T> List<T> multiGetForHash(RedisKeyBuild redisKeyBuild, List<String> hashKeys, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotBlank(hashKeys);
        String key = redisKeyBuild.getRealKey();
        List<Object> objHashKeys = new ArrayList<>(hashKeys);
        List<Object> multiGetObj = redisTemplate.opsForHash().multiGet(key, objHashKeys);

        if (CacheUtil.checkRedisListIsEmpty(multiGetObj)){
            return new ArrayList<>();
        }
        if (String.class.isAssignableFrom(clazz)) {
            return (List<T>) multiGetObj;
        }

        return parseObjects(multiGetObj, clazz);
    }

    /**
     * 获取 Hash Key 下所有值
     *
     * @param redisKeyBuild 缓存key
     * @param clazz         类型
     * @return
     */
    @Override
    public <T> List<T> getAllForHash(RedisKeyBuild redisKeyBuild, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        List<Object> valuesObj = this.redisTemplate.opsForHash().values(key);
        if (CacheUtil.checkRedisListIsEmpty(valuesObj)){
            return new ArrayList<>();
        }
        if (String.class.isAssignableFrom(clazz)) {
            return (List<T>) valuesObj;
        }

        return parseObjects(valuesObj, clazz);
    }

    /**
     * 获取 Hash Key 下所有值，返回值为map
     *
     * @param redisKeyBuild 缓存key
     * @param clazz         类型
     * @return
     */
    @Override
    public <T> Map<String, T> getAllMapForHash(RedisKeyBuild redisKeyBuild, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        Map<Object, Object> entries = this.redisTemplate.opsForHash().entries(key);
        Map<String,T> map = new HashMap<>(64);
        entries.forEach((k,v) -> {
            map.put(String.valueOf(k),getComplex(v, clazz));
        });
        return map;
    }

    /**
     * 获取 Hash Key 下所有值，返回值为map
     *
     * @param key   缓存key
     * @param clazz 类型
     * @return
     */
    @Override
    public <T> Map<String, T> getAllMapForHash(String key, Class<T> clazz) {
        Map<Object, Object> entries = this.redisTemplate.opsForHash().entries(key);
        Map<String,T> map = new HashMap<>(64);
        entries.forEach((k,v) -> {
            map.put(String.valueOf(k),getComplex(v, clazz));
        });
        return map;
    }

    /**
     * 判断hash中 key是否存在
     *
     * @param redisKeyBuild 缓存key
     * @param hashKey       hash中key
     * @return 结果
     */
    @Override
    public Boolean hasKeyForHash(RedisKeyBuild redisKeyBuild, String hashKey) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotBlank(hashKey);
        String key = redisKeyBuild.getRealKey();
        return this.redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    /**
     * 删除hash key
     *
     * @param redisKeyBuild 缓存key
     * @param hashKey       hash中key
     * @return 结果
     */
    @Override
    public Long delForHash(RedisKeyBuild redisKeyBuild, String hashKey) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotBlank(hashKey);
        String key = redisKeyBuild.getRealKey();
        return this.redisTemplate.opsForHash().delete(key, hashKey);
    }

    /**
     * 批量删除hash key
     *
     * @param redisKeyBuild 缓存key
     * @param hashKeys      hash中key
     * @return 结果
     */
    @Override
    public Long delForHash(RedisKeyBuild redisKeyBuild, Collection<String> hashKeys) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotBlank(hashKeys);
        String key = redisKeyBuild.getRealKey();
        return this.redisTemplate.opsForHash().delete(key, hashKeys.toArray());
    }

    /**
     * 为哈希表 key 中的指定字段的整数值加上增量 increment
     *
     * @param redisKeyBuild 缓存key
     * @param hashKey       hash中key
     * @param increment     步长
     * @return 结果
     */
    @Override
    public Long incrByForHash(RedisKeyBuild redisKeyBuild, String hashKey, long increment) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotBlank(hashKey);
        String key = redisKeyBuild.getRealKey();
        return this.redisTemplate.opsForHash().increment(key, hashKey, increment);
    }

    /**
     * 为哈希表 key 中的指定字段的整数值加上增量 increment(double类型)
     *
     * @param redisKeyBuild 缓存key
     * @param hashKey       hash中key
     * @param delta         步长
     * @return 结果
     */
    @Override
    public Double incrByDoubleForHash(RedisKeyBuild redisKeyBuild, String hashKey, double delta) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotBlank(hashKey);
        String key = redisKeyBuild.getRealKey();
        return this.redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    /**
     * 获取所有哈希表中的hashKey
     *
     * @param redisKeyBuild 缓存key
     * @return 结果
     */
    @Override
    public Set<String> hashKeysForHash(RedisKeyBuild redisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        Set<Object> keys = this.redisTemplate.opsForHash().keys(key);
        return parseObjects(keys,String.class);
    }

    /**
     * 获取哈希表中字段的数量
     *
     * @param redisKeyBuild 缓存key
     * @return 结果
     */
    @Override
    public Long sizeForHash(RedisKeyBuild redisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        return this.redisTemplate.opsForHash().size(key);
    }

    /**
     * 通过索引获取列表中的元素
     *
     * @param redisKeyBuild 缓存key
     * @param index         索引
     * @param clazz         类型
     * @return 结果
     */
    @Override
    public <T> T indexForList(RedisKeyBuild redisKeyBuild, long index, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        String cachedValue = this.redisTemplate.opsForList().index(key, index);
        if (StringUtils.isEmpty(cachedValue)) {
            return null;
        }
        if (String.class.isAssignableFrom(clazz)) {
            return (T) cachedValue;
        }
        return getComplex(cachedValue, clazz);
    }

    /**
     * List 从左边放入元素
     *
     * @param redisKeyBuild key
     * @param value         value
     * @return 改动行数
     */
    @Override
    public Long leftPushForList(RedisKeyBuild redisKeyBuild, Object value) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(value);
        String key = redisKeyBuild.getRealKey();
        String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
        return this.redisTemplate.opsForList().leftPush(key, jsonValue);
    }

    /**
     * List 从左边放入元素
     *
     * @param redisKeyBuild key
     * @param valueList     valueList
     * @return 改动行数
     */
    @Override
    public Long leftPushAllForList(RedisKeyBuild redisKeyBuild, List<?> valueList) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(valueList);
        String key = redisKeyBuild.getRealKey();
        List<String> jsonList = new ArrayList<>(valueList.size());
        valueList.forEach(value -> {
            String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
            jsonList.add(jsonValue);
        });
        return this.redisTemplate.opsForList().leftPushAll(key, jsonList);
    }

    /**
     * List 从左边放入元素(当list存在的时候才加入)
     *
     * @param redisKeyBuild
     * @param value
     * @return
     */
    @Override
    public Long leftPushIfPresentForList(RedisKeyBuild redisKeyBuild, Object value) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(value);
        String key = redisKeyBuild.getRealKey();
        String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
        return this.redisTemplate.opsForList().leftPushIfPresent(key, jsonValue);
    }

    /**
     * 如果pivot存在,在pivot左边添加
     *
     * @param redisKeyBuild 缓存key
     * @param pivot         pivot
     * @param value         对象
     * @return 结果
     */
    @Override
    public Long leftPushForList(RedisKeyBuild redisKeyBuild, Object pivot, Object value) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(pivot);
        CacheUtil.checkNotEmpty(value);
        String key = redisKeyBuild.getRealKey();
        String jsonPivot = value instanceof String ? (String) pivot : JSON.toJSONString(pivot);
        String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
        return this.redisTemplate.opsForList().leftPush(key, jsonPivot, jsonValue);
    }

    /**
     * List 从右边放入元素
     *
     * @param redisKeyBuild key
     * @param value         value
     * @return 改动行数
     */
    @Override
    public Long rightPushForList(RedisKeyBuild redisKeyBuild, Object value) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(value);
        String key = redisKeyBuild.getRealKey();
        String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
        return this.redisTemplate.opsForList().rightPush(key, jsonValue);
    }

    /**
     * List 从右边放入元素
     *
     * @param redisKeyBuild key
     * @param valueList     valueList
     * @return 改动行数
     */
    @Override
    public Long rightPushAllForList(RedisKeyBuild redisKeyBuild, List<Object> valueList) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(valueList);
        String key = redisKeyBuild.getRealKey();
        List<String> jsonList = new ArrayList<>(valueList.size());
        valueList.forEach(value -> {
            String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
            jsonList.add(jsonValue);
        });
        return this.redisTemplate.opsForList().rightPushAll(key, jsonList);
    }

    /**
     * List 从右边放入元素(当list存在的时候才加入)
     *
     * @param redisKeyBuild 缓存key
     * @param value         对象
     * @return 结果
     */
    @Override
    public Long rightPushIfPresentForList(RedisKeyBuild redisKeyBuild, Object value) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(value);
        String key = redisKeyBuild.getRealKey();
        String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
        return this.redisTemplate.opsForList().rightPushIfPresent(key, jsonValue);
    }

    /**
     * 如果pivot存在,在pivot右边添加
     *
     * @param redisKeyBuild 缓存key
     * @param pivot         pivot
     * @param value         对象
     * @return 结果
     */
    @Override
    public Long rightPushForList(RedisKeyBuild redisKeyBuild, Object pivot, Object value) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(pivot);
        CacheUtil.checkNotEmpty(value);
        String key = redisKeyBuild.getRealKey();
        String jsonPivot = value instanceof String ? (String) pivot : JSON.toJSONString(pivot);
        String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
        return this.redisTemplate.opsForList().rightPush(key, jsonPivot, jsonValue);
    }

    /**
     * 通过索引设置列表元素的值
     *
     * @param redisKeyBuild 缓存key
     * @param index         位置
     * @param value         对象
     */
    @Override
    public void setForList(RedisKeyBuild redisKeyBuild, long index, Object value) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(value);
        String key = redisKeyBuild.getRealKey();
        String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
        this.redisTemplate.opsForList().set(key, index, jsonValue);
    }

    /**
     * 移出并获取列表的第一个元素
     *
     * @param redisKeyBuild 缓存key
     * @param clazz         类型
     * @return 删除的元素
     */
    @Override
    public <T> T leftPopForList(RedisKeyBuild redisKeyBuild, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        String s = this.redisTemplate.opsForList().leftPop(key);
        return getComplex(s, clazz);
    }

    /**
     * 移出并获取列表的第一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
     *
     * @param redisKeyBuild 缓存key
     * @param clazz         类型
     * @param timeout       等待时间
     * @param unit          时间单位
     * @return
     */
    @Override
    public <T> T leftPopBlockForList(RedisKeyBuild redisKeyBuild, Class<T> clazz, long timeout, TimeUnit unit) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        String s = this.redisTemplate.opsForList().leftPop(key, timeout, unit);
        return getComplex(s, clazz);
    }

    /**
     * 移除并获取列表最后一个元素
     *
     * @param redisKeyBuild 缓存key
     * @param clazz         类型
     * @return 删除的元素
     */
    @Override
    public <T> T rightPopForList(RedisKeyBuild redisKeyBuild, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        String s = this.redisTemplate.opsForList().rightPop(key);
        return getComplex(s, clazz);
    }

    /**
     * 移出并获取列表的最后一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
     *
     * @param redisKeyBuild 缓存key
     * @param clazz         类型
     * @param timeout       等待时间
     * @param unit          时间单位
     * @return
     */
    @Override
    public <T> T rightPopBlockForList(RedisKeyBuild redisKeyBuild, Class<T> clazz, long timeout, TimeUnit unit) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        String s = this.redisTemplate.opsForList().rightPop(key, timeout, unit);
        return getComplex(s, clazz);
    }

    /**
     * 移除列表的最后一个元素，并将该元素添加到另一个列表并返回
     *
     * @param sourceKey
     * @param destinationKey
     * @param clazz
     * @return
     */
    @Override
    public <T> T rightPopAndLeftPushForList(RedisKeyBuild sourceKey, RedisKeyBuild destinationKey, Class<T> clazz) {
        CacheUtil.checkNotBlank(sourceKey);
        CacheUtil.checkNotBlank(destinationKey);
        String sourceRelKey = sourceKey.getRealKey();
        String destinationRelKey = destinationKey.getRealKey();
        String s = this.redisTemplate.opsForList().rightPopAndLeftPush(sourceRelKey, destinationRelKey);
        return getComplex(s, clazz);
    }

    /**
     * 从列表中弹出一个值，将弹出的元素插入到另外一个列表中并返回它； 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
     *
     * @param sourceKey      缓存key
     * @param destinationKey 弹出key
     * @param clazz          类型
     * @param timeout        时间
     * @param unit           时间单位
     * @return 结果
     */
    @Override
    public <T> T rightPopBlockAndLeftPushForList(RedisKeyBuild sourceKey, RedisKeyBuild destinationKey, Class<T> clazz, long timeout, TimeUnit unit) {
        CacheUtil.checkNotBlank(sourceKey);
        CacheUtil.checkNotBlank(destinationKey);
        String sourceRelKey = sourceKey.getRealKey();
        String destinationRelKey = destinationKey.getRealKey();
        String s = this.redisTemplate.opsForList().rightPopAndLeftPush(sourceRelKey, destinationRelKey, timeout, unit);
        return getComplex(s, clazz);
    }

    /**
     * 获取 List 全部数据
     *
     * @param redisKeyBuild 缓存key
     * @param clazz         类型
     * @return
     */
    @Override
    public <T> List<T> getAllForList(RedisKeyBuild redisKeyBuild, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        List list = this.redisTemplate.opsForList().range(key, 0, -1);
        if (CacheUtil.checkRedisListIsEmpty(list)){
            return new ArrayList<>();
        }
        return parseObjects(list, clazz);
    }

    /**
     * 获取列表指定范围内的元素
     *
     * @param redisKeyBuild 缓存key
     * @param start         开始位置, 0是开始位置
     * @param end           结束位置, -1返回所有
     * @param clazz         类型
     * @return 结果
     */
    @Override
    public <T> List<T> rangeForList(RedisKeyBuild redisKeyBuild, long start, long end, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        List range = this.redisTemplate.opsForList().range(key, start, end);
        if (CacheUtil.checkRedisListIsEmpty(range)){
            return new ArrayList<>();
        }
        return parseObjects(range, clazz);
    }

    /**
     * 删除集合中值等于value得元素
     *
     * @param redisKeyBuild 缓存key
     * @param index         index=0, 删除所有值等于value的元素; index>0, 从头部开始删除第一个值等于value的元素;
     *                      index<0, 从尾部开始删除第一个值等于value的元素;
     * @param value         对象
     * @return 结果
     */
    @Override
    public Long removeForList(RedisKeyBuild redisKeyBuild, long index, Object value) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
        return this.redisTemplate.opsForList().remove(key, index, jsonValue);
    }

    /**
     * 裁剪list
     *
     * @param redisKeyBuild
     * @param start
     * @param end
     */
    @Override
    public void trimForList(RedisKeyBuild redisKeyBuild, long start, long end) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        this.redisTemplate.opsForList().trim(key, start, end);
    }

    /**
     * 获取列表长度
     *
     * @param redisKeyBuild
     * @return
     */
    @Override
    public Long lenForList(RedisKeyBuild redisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        return this.redisTemplate.opsForList().size(key);
    }

    /**
     * set添加元素
     *
     * @param redisKeyBuild
     * @param value
     * @return
     */
    @Override
    public Long addForSet(RedisKeyBuild redisKeyBuild, Object value) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
        return this.redisTemplate.opsForSet().add(key, jsonValue);
    }

    /**
     * set批量添加元素
     *
     * @param redisKeyBuild
     * @param values
     * @return
     */
    @Override
    public Long addForSet(RedisKeyBuild redisKeyBuild, List<?> values) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(values);
        String key = redisKeyBuild.getRealKey();
        List<String> jsonList = new ArrayList<>(values.size());
        values.forEach(value -> {
            String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
            jsonList.add(jsonValue);
        });
        return this.redisTemplate.opsForSet().add(key, jsonList.toArray(new String[]{}));
    }

    /**
     * set移除元素
     *
     * @param redisKeyBuild
     * @param value
     * @return
     */
    @Override
    public Long removeForSet(RedisKeyBuild redisKeyBuild, Object value) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(value);
        String key = redisKeyBuild.getRealKey();
        String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
        return this.redisTemplate.opsForSet().remove(key, jsonValue);
    }

    /**
     * set批量移除元素
     *
     * @param redisKeyBuild
     * @param values
     * @return
     */
    @Override
    public Long removeForSet(RedisKeyBuild redisKeyBuild, List<?> values) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(values);
        String key = redisKeyBuild.getRealKey();
        List<String> jsonList = values.stream()
                .map(value -> value instanceof String ? (String) value : JSON.toJSONString(value))
                .collect(Collectors.toList());
        return this.redisTemplate.opsForSet().remove(key, jsonList.toArray(new String[]{}));
    }

    /**
     * 移除并返回集合的一个随机元素
     *
     * @param redisKeyBuild
     * @param clazz
     * @return
     */
    @Override
    public <T> T popForSet(RedisKeyBuild redisKeyBuild, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        String cachedValue = this.redisTemplate.opsForSet().pop(key);
        return getComplex(cachedValue,clazz);
    }

    /**
     * 将元素value从一个集合移到另一个集合
     *
     * @param redisKeyBuild
     * @param value
     * @param destRedisKeyBuild
     * @return
     */
    @Override
    public boolean moveForSet(RedisKeyBuild redisKeyBuild, Object value, RedisKeyBuild destRedisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(value);
        CacheUtil.checkNotBlank(destRedisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        String destKey = destRedisKeyBuild.getRealKey();
        String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
        return this.redisTemplate.opsForSet().move(key, jsonValue, destKey);
    }

    /**
     * 获取集合的大小
     *
     * @param redisKeyBuild
     * @return
     */
    @Override
    public Long sizeForSet(RedisKeyBuild redisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        return this.redisTemplate.opsForSet().size(key);
    }

    /**
     * 判断集合是否包含value
     *
     * @param redisKeyBuild
     * @param value
     * @return
     */
    @Override
    public Boolean isMemberForSet(RedisKeyBuild redisKeyBuild, Object value) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(value);
        String key = redisKeyBuild.getRealKey();
        String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
        return this.redisTemplate.opsForSet().isMember(key, jsonValue);
    }

    /**
     * 获取两个集合的交集
     *
     * @param redisKeyBuild
     * @param otherRedisKeyBuild
     * @param clazz
     * @return
     */
    @Override
    public <T> Set<T> intersectForSet(RedisKeyBuild redisKeyBuild, RedisKeyBuild otherRedisKeyBuild, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotBlank(otherRedisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        String otherKey = otherRedisKeyBuild.getRealKey();
        Set set = this.redisTemplate.opsForSet().intersect(key, otherKey);
        return parseObjects(set,clazz);
    }

    /**
     * 获取key集合与多个集合的交集
     *
     * @param redisKeyBuild
     * @param otherRedisKeyBuilds
     * @param clazz
     * @return
     */
    @Override
    public <T> Set<T> intersectForSet(RedisKeyBuild redisKeyBuild, Collection<RedisKeyBuild> otherRedisKeyBuilds, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(otherRedisKeyBuilds);
        String key = redisKeyBuild.getRealKey();
        List<String> otherKeys = CacheUtil.getBatchKey(otherRedisKeyBuilds);
        Set set = this.redisTemplate.opsForSet().intersect(key, otherKeys);
        return parseObjects(set,clazz);
    }

    /**
     * key集合与otherKey集合的交集存储到destKey集合中
     *
     * @param redisKeyBuild
     * @param otherRedisKeyBuild
     * @param destRedisKeyBuild
     * @return
     */
    @Override
    public Long intersectAndStoreForSet(RedisKeyBuild redisKeyBuild, RedisKeyBuild otherRedisKeyBuild, RedisKeyBuild destRedisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotBlank(otherRedisKeyBuild);
        CacheUtil.checkNotBlank(destRedisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        String otherKey = otherRedisKeyBuild.getRealKey();
        String destKey = destRedisKeyBuild.getRealKey();
        return this.redisTemplate.opsForSet().intersectAndStore(key, otherKey, destKey);
    }

    /**
     * key集合与多个集合的交集存储到destKey集合中
     *
     * @param redisKeyBuild
     * @param otherRedisKeyBuilds
     * @param destRedisKeyBuild
     * @return
     */
    @Override
    public Long intersectAndStoreForSet(RedisKeyBuild redisKeyBuild, Collection<RedisKeyBuild> otherRedisKeyBuilds, RedisKeyBuild destRedisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(otherRedisKeyBuilds);
        CacheUtil.checkNotBlank(destRedisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        List<String> otherKeys = CacheUtil.getBatchKey(otherRedisKeyBuilds);
        String destKey = destRedisKeyBuild.getRealKey();
        return this.redisTemplate.opsForSet().intersectAndStore(key, otherKeys, destKey);
    }

    /**
     * 获取两个集合的并集
     *
     * @param redisKeyBuild
     * @param otherRedisKeyBuild
     * @param clazz
     * @return
     */
    @Override
    public <T> Set<T> unionForSet(RedisKeyBuild redisKeyBuild, RedisKeyBuild otherRedisKeyBuild, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotBlank(otherRedisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        String otherKey = otherRedisKeyBuild.getRealKey();
        Set set = this.redisTemplate.opsForSet().union(key, otherKey);
        return parseObjects(set,clazz);
    }

    /**
     * 获取key集合与多个集合的并集
     *
     * @param redisKeyBuild
     * @param otherRedisKeyBuilds
     * @param clazz
     * @return
     */
    @Override
    public <T> Set<T> unionForSet(RedisKeyBuild redisKeyBuild, Collection<RedisKeyBuild> otherRedisKeyBuilds, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(otherRedisKeyBuilds);
        String key = redisKeyBuild.getRealKey();
        List<String> otherKeys = CacheUtil.getBatchKey(otherRedisKeyBuilds);
        Set set = this.redisTemplate.opsForSet().union(key, otherKeys);
        return parseObjects(set,clazz);
    }

    /**
     * key集合与otherKey集合的并集存储到destKey中
     *
     * @param redisKeyBuild
     * @param otherRedisKeyBuild
     * @param destRedisKeyBuild
     * @return
     */
    @Override
    public Long unionAndStoreForSet(RedisKeyBuild redisKeyBuild, RedisKeyBuild otherRedisKeyBuild, RedisKeyBuild destRedisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotBlank(otherRedisKeyBuild);
        CacheUtil.checkNotBlank(destRedisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        String otherKey = otherRedisKeyBuild.getRealKey();
        String destKey = destRedisKeyBuild.getRealKey();
        return this.redisTemplate.opsForSet().unionAndStore(key, otherKey, destKey);
    }

    /**
     * key集合与多个集合的并集存储到destKey中
     *
     * @param redisKeyBuild
     * @param otherRedisKeyBuilds
     * @param destRedisKeyBuild
     * @return
     */
    @Override
    public Long unionAndStoreForSet(RedisKeyBuild redisKeyBuild, Collection<RedisKeyBuild> otherRedisKeyBuilds, RedisKeyBuild destRedisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(otherRedisKeyBuilds);
        CacheUtil.checkNotBlank(destRedisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        List<String> otherKeys = CacheUtil.getBatchKey(otherRedisKeyBuilds);
        String destKey = destRedisKeyBuild.getRealKey();
        return this.redisTemplate.opsForSet().unionAndStore(key, otherKeys, destKey);
    }

    /**
     * 获取两个集合的差集
     *
     * @param redisKeyBuild
     * @param otherRedisKeyBuild
     * @param clazz
     * @return
     */
    @Override
    public <T> Set<T> differenceForSet(RedisKeyBuild redisKeyBuild, RedisKeyBuild otherRedisKeyBuild, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotBlank(otherRedisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        String otherKey = otherRedisKeyBuild.getRealKey();
        Set set = this.redisTemplate.opsForSet().difference(key, otherKey);
        return parseObjects(set,clazz);
    }

    /**
     * 获取key集合与多个集合的差集
     *
     * @param redisKeyBuild
     * @param otherRedisKeyBuilds
     * @param clazz
     * @return
     */
    @Override
    public <T> Set<T> differenceForSet(RedisKeyBuild redisKeyBuild, Collection<RedisKeyBuild> otherRedisKeyBuilds, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(otherRedisKeyBuilds);
        String key = redisKeyBuild.getRealKey();
        List<String> otherKeys = CacheUtil.getBatchKey(otherRedisKeyBuilds);
        Set set = this.redisTemplate.opsForSet().difference(key, otherKeys);
        return parseObjects(set,clazz);
    }

    /**
     * key集合与otherKey集合的差集存储到destKey中
     *
     * @param redisKeyBuild
     * @param otherRedisKeyBuild
     * @param destRedisKeyBuild
     * @return
     */
    @Override
    public Long differenceForSet(RedisKeyBuild redisKeyBuild, RedisKeyBuild otherRedisKeyBuild, RedisKeyBuild destRedisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotBlank(otherRedisKeyBuild);
        CacheUtil.checkNotBlank(destRedisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        String otherKey = otherRedisKeyBuild.getRealKey();
        String destKey = destRedisKeyBuild.getRealKey();
        return this.redisTemplate.opsForSet().differenceAndStore(key, otherKey, destKey);
    }

    /**
     * key集合与多个集合的差集存储到destKey中
     *
     * @param redisKeyBuild
     * @param otherRedisKeyBuilds
     * @param destRedisKeyBuild
     * @return
     */
    @Override
    public Long differenceForSet(RedisKeyBuild redisKeyBuild, Collection<RedisKeyBuild> otherRedisKeyBuilds, RedisKeyBuild destRedisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(otherRedisKeyBuilds);
        CacheUtil.checkNotBlank(destRedisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        List<String> otherKeys = CacheUtil.getBatchKey(otherRedisKeyBuilds);
        String destKey = destRedisKeyBuild.getRealKey();
        return this.redisTemplate.opsForSet().differenceAndStore(key, otherKeys, destKey);
    }

    /**
     * 获取集合所有元素
     *
     * @param redisKeyBuild
     * @param clazz
     * @return
     */
    @Override
    public <T> Set<T> membersForSet(RedisKeyBuild redisKeyBuild, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        Set members = this.redisTemplate.opsForSet().members(key);
        return parseObjects(members,clazz);
    }

    /**
     * 随机获取集合中的一个元素
     *
     * @param redisKeyBuild
     * @param clazz
     * @return
     */
    @Override
    public <T> T randomMemberForSet(RedisKeyBuild redisKeyBuild, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        String s = this.redisTemplate.opsForSet().randomMember(key);
        return getComplex(s, clazz);
    }

    /**
     * 随机获取集合中count个元素
     *
     * @param redisKeyBuild
     * @param count
     * @param clazz
     * @return
     */
    @Override
    public <T> List<T> randomMembersForSet(RedisKeyBuild redisKeyBuild, long count, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        List list = this.redisTemplate.opsForSet().randomMembers(key, count);
        if (CacheUtil.checkRedisListIsEmpty(list)){
            return new ArrayList<>();
        }
        return parseObjects(list,clazz);
    }

    /**
     * 随机获取集合中count个元素并且去除重复的
     *
     * @param redisKeyBuild
     * @param count
     * @param clazz
     * @return
     */
    @Override
    public <T> Set<T> distinctRandomMembersForSet(RedisKeyBuild redisKeyBuild, long count, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        Set set = this.redisTemplate.opsForSet().distinctRandomMembers(key, count);
        return parseObjects(set,clazz);
    }

    /**
     * 游标遍历
     *
     * @param redisKeyBuild
     * @param options
     * @return
     */
    @Override
    public Cursor<String> scanForSet(RedisKeyBuild redisKeyBuild, ScanOptions options) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        Cursor<String> scan = this.redisTemplate.opsForSet().scan(key, options);
        return scan;
    }

    /**
     * 存储有序列表
     *
     * @param redisKeyBuild key
     * @param value         value
     * @param score         评分值
     * @return
     */
    @Override
    public void addForSortedSet(RedisKeyBuild redisKeyBuild, Object value, Double score) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(value);
        CacheUtil.checkNotEmpty(score);
        String key = redisKeyBuild.getRealKey();
        String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
        this.redisTemplate.opsForZSet().add(key,jsonValue,score);
    }

    /**
     * 存储有序列表并设置超时时间(秒)
     *
     * @param redisKeyBuild key
     * @param value         value
     * @param score         评分值
     * @param ttl           超时时间
     * @return
     */
    @Override
    public void addForSortedSet(RedisKeyBuild redisKeyBuild, Object value, Double score, long ttl) {
        addForSortedSet(redisKeyBuild,value,score,ttl, CacheUtil.DEFAULT_TIME_UNIT);
    }

    /**
     * 存储有序列表并设置超时时间
     *
     * @param redisKeyBuild key
     * @param value         value
     * @param score         评分值
     * @param ttl           超时时间
     * @param timeUnit      时间单位
     * @return
     */
    @Override
    public void addForSortedSet(RedisKeyBuild redisKeyBuild, Object value, Double score, long ttl, TimeUnit timeUnit) {
        addForSortedSet(redisKeyBuild,value,score);
        expire(redisKeyBuild, ttl, timeUnit);
    }

    /**
     * 存储有序列表
     *
     * @param redisKeyBuild
     * @param map           map中的key如果是自定义对象类型需要重新equals和hashcode方法
     * @return
     */
    @Override
    public Long addForSortedSet(RedisKeyBuild redisKeyBuild, Map<?, Double> map) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        Set<ZSetOperations.TypedTuple<String>> collect =
                map.entrySet()
                        .stream()
                        .map(item -> {
                            String entryKey = item.getKey() instanceof String ? (String) item.getKey() : JSON.toJSONString(item.getKey());
                            return new DefaultTypedTuple<>(entryKey, item.getValue());
                        })
                        .collect(Collectors.toSet());
        return this.redisTemplate.opsForZSet().add(key, collect);
    }

    /**
     * 存储有序列表并设置超时时间(秒)
     *
     * @param redisKeyBuild
     * @param map           map中的key如果是自定义对象类型需要重新equals和hashcode方法
     * @param ttl
     * @return
     */
    @Override
    public Long addForSortedSet(RedisKeyBuild redisKeyBuild, Map<?, Double> map, long ttl) {
        return addForSortedSet(redisKeyBuild, map, ttl, CacheUtil.DEFAULT_TIME_UNIT);
    }

    /**
     * 存储有序列表并设置超时时间 自定义单位
     *
     * @param redisKeyBuild key
     * @param map           map中的key如果是自定义对象类型需要重新equals和hashcode方法
     * @param ttl           过期时间
     * @param timeUnit      过期时间单位
     * @return 影响的条目
     */
    @Override
    public Long addForSortedSet(RedisKeyBuild redisKeyBuild, Map<?, Double> map, long ttl, TimeUnit timeUnit) {
        Long count = addForSortedSet(redisKeyBuild, map);
        expire(redisKeyBuild, ttl, timeUnit);
        return count;
    }

    /**
     * 获取有序列表中范围条目,并转为指定类型
     *
     * @param redisKeyBuild key
     * @param start         开始下标 从0开始
     * @param end           结束下标 包含此条
     * @param clazz         序列化类型
     * @return 结果set集合
     */
    @Override
    public <T> Set<T> getRangeForSortedSet(RedisKeyBuild redisKeyBuild, long start, long end, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        Set resultSet = this.redisTemplate.opsForZSet().range(key, start, end);
        return parseObjects(resultSet,clazz);
    }

    /**
     * 反转获取有序列表中范围条目,并转为指定类型
     *
     * @param redisKeyBuild key
     * @param start         开始下标 从0开始
     * @param end           结束下标 包含此条
     * @param clazz         序列化类型
     * @return 结果set集合
     */
    @Override
    public <T> Set<T> getReverseRangeForSortedSet(RedisKeyBuild redisKeyBuild, long start, long end, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        Set resultSet = this.redisTemplate.opsForZSet().reverseRange(key, start, end);
        return parseObjects(resultSet,clazz);
    }

    /**
     * 删除zSet条目
     *
     * @param redisKeyBuild key
     * @param value         数据
     * @return 影响条目
     */
    @Override
    public Long delForSortedSet(RedisKeyBuild redisKeyBuild, Object value) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
        return this.redisTemplate.opsForZSet().remove(key, jsonValue);
    }

    /**
     * 批量删除zSet条目
     *
     * @param redisKeyBuild   key
     * @param valueCollection 数据
     * @return 影响条目
     */
    @Override
    public Long delForSortedSet(RedisKeyBuild redisKeyBuild, Collection<?> valueCollection) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(valueCollection);
        String key = redisKeyBuild.getRealKey();
        List<String> jsonValueList = valueCollection.stream()
                .map(value -> value instanceof String ? (String) value : JSON.toJSONString(value))
                .distinct()
                .collect(Collectors.toList());
        return this.redisTemplate.opsForZSet().remove(key,jsonValueList.toArray());
    }

    /**
     * 删除范围元素
     *
     * @param redisKeyBuild key
     * @param start         开始range
     * @param end           结束range
     * @return 影响条目
     */
    @Override
    public Long delRangeForSortedSet(RedisKeyBuild redisKeyBuild, long start, long end) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        return this.redisTemplate.opsForZSet().removeRange(key, start, end);
    }

    /**
     * 增加元素的score值，并返回增加后的值
     *
     * @param redisKeyBuild key
     * @param value         对象
     * @param delta         值
     * @return 结果
     */
    @Override
    public Double incrementScoreForSortedSet(RedisKeyBuild redisKeyBuild, Object value, double delta) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(value);
        String key = redisKeyBuild.getRealKey();
        String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
        return this.redisTemplate.opsForZSet().incrementScore(key, jsonValue, delta);
    }

    /**
     * 计算zSet总条数
     *
     * @param redisKeyBuild key
     * @return 总条数  不存在则空
     */
    @Override
    public Long sizeForSortedSet(RedisKeyBuild redisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * 返回元素在集合的排名,有序集合是按照元素的score值由小到大排列
     *
     * @param redisKeyBuild key
     * @param value         value
     * @return rank
     */
    @Override
    public Long rankForSortedSet(RedisKeyBuild redisKeyBuild, Object value) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(value);
        String key = redisKeyBuild.getRealKey();
        String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
        return this.redisTemplate.opsForZSet().rank(key, jsonValue);
    }

    /**
     * 返回元素在集合的排名,按元素的score值由大到小排列
     *
     * @param redisKeyBuild
     * @param value
     * @return
     */
    @Override
    public Long reverseRankForSortedSet(RedisKeyBuild redisKeyBuild, Object value) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(value);
        String key = redisKeyBuild.getRealKey();
        String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
        return this.redisTemplate.opsForZSet().reverseRank(key, jsonValue);
    }

    /**
     * 获取集合元素, 并且把score值也获取
     *
     * @param redisKeyBuild
     * @param start
     * @param end
     * @param clazz
     * @return
     */
    @Override
    public <T> Set<ZSetOperations.TypedTuple<T>> rangeWithScoreForSortedSet(RedisKeyBuild redisKeyBuild, long start, long end, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        Set<ZSetOperations.TypedTuple<String>> cacheSet = this.redisTemplate.opsForZSet().rangeWithScores(key, start, end);
        if (cacheSet == null) {
            return new HashSet<>();
        }
        return typedTupleStringParseObjects(cacheSet, clazz);
    }

    /**
     * 根据Score值查询集合元素
     *
     * @param redisKeyBuild
     * @param min           最小值
     * @param max           最大值
     * @param clazz
     * @return
     */
    @Override
    public <T> Set<T> rangeByScoreForSortedSet(RedisKeyBuild redisKeyBuild, double min, double max, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        Set set = this.redisTemplate.opsForZSet().rangeByScore(key, min, max);
        return parseObjects(set,clazz);
    }

    /**
     * 根据Score值查询集合元素(含有score值), 从小到大排序
     *
     * @param redisKeyBuild
     * @param min           最小值
     * @param max           最大值
     * @param clazz
     * @return
     */
    @Override
    public <T> Set<ZSetOperations.TypedTuple<T>> rangeByScoreWithScoreForSortedSet(RedisKeyBuild redisKeyBuild, double min, double max, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        Set<ZSetOperations.TypedTuple<String>> cacheSet = this.redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
        return typedTupleStringParseObjects(cacheSet, clazz);
    }

    /**
     * 根据Score值查询集合元素, 从小到大排序
     *
     * @param redisKeyBuild
     * @param min           最小值
     * @param max           最大值
     * @param start
     * @param end
     * @param clazz
     * @return
     */
    @Override
    public <T> Set<ZSetOperations.TypedTuple<T>> rangeByScoreWithScoreForSortedSet(RedisKeyBuild redisKeyBuild, double min, double max, long start, long end, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        Set<ZSetOperations.TypedTuple<String>> cacheSet = this.redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max, start, end);
        return typedTupleStringParseObjects(cacheSet, clazz);
    }

    /**
     * 获取集合的元素, 从大到小排序, 并返回score值
     *
     * @param redisKeyBuild
     * @param start
     * @param end
     * @param clazz
     * @return
     */
    @Override
    public <T> Set<ZSetOperations.TypedTuple<T>> reverseRangeWithScoreForSortedSet(RedisKeyBuild redisKeyBuild, long start, long end, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        Set<ZSetOperations.TypedTuple<String>> cacheSet = this.redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        return typedTupleStringParseObjects(cacheSet, clazz);
    }

    /**
     * 根据Score值查询集合元素, 从大到小排序
     *
     * @param redisKeyBuild
     * @param min
     * @param max
     * @param clazz
     * @return
     */
    @Override
    public <T> Set<T> reverseRangeByScoreForSortedSet(RedisKeyBuild redisKeyBuild, double min, double max, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        Set set = this.redisTemplate.opsForZSet().reverseRangeByScore(key, min, max);
        return parseObjects(set,clazz);
    }

    /**
     * 根据Score值查询集合元素, 从大到小排序, 并返回score值
     *
     * @param redisKeyBuild
     * @param min
     * @param max
     * @param clazz
     * @return
     */
    @Override
    public <T> Set<ZSetOperations.TypedTuple<T>> reverseRangeByScoreWithScoreForSortedSet(RedisKeyBuild redisKeyBuild, double min, double max, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        Set<ZSetOperations.TypedTuple<String>> cacheSet = this.redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, min, max);
        return typedTupleStringParseObjects(cacheSet, clazz);
    }

    /**
     * 根据Score值查询集合元素, 从大到小排序
     *
     * @param redisKeyBuild
     * @param min
     * @param max
     * @param start
     * @param end
     * @param clazz
     * @return
     */
    @Override
    public <T> Set<T> reverseRangeByScoreForSortedSet(RedisKeyBuild redisKeyBuild, double min, double max, long start, long end, Class<T> clazz) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        Set set = this.redisTemplate.opsForZSet().reverseRangeByScore(key, min, max, start, end);
        return parseObjects(set, clazz);
    }

    /**
     * 根据score值获取集合元素数量
     *
     * @param redisKeyBuild
     * @param min
     * @param max
     * @return
     */
    @Override
    public Long countForSortedSet(RedisKeyBuild redisKeyBuild, double min, double max) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        return this.redisTemplate.opsForZSet().count(key, min, max);
    }

    /**
     * 获取集合大小
     *
     * @param redisKeyBuild
     * @return
     */
    @Override
    public Long zCardForSortedSet(RedisKeyBuild redisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        return this.redisTemplate.opsForZSet().zCard(key);
    }

    /**
     * 获取集合中value元素的score值
     *
     * @param redisKeyBuild
     * @param value
     * @return
     */
    @Override
    public Double scoreByValueForSortedSet(RedisKeyBuild redisKeyBuild, Object value) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(value);
        String key = redisKeyBuild.getRealKey();
        String jsonValue = value instanceof String ? (String) value : JSON.toJSONString(value);
        return this.redisTemplate.opsForZSet().score(key, jsonValue);
    }

    /**
     * 移除指定索引位置的成员
     *
     * @param redisKeyBuild
     * @param start
     * @param end
     * @return
     */
    @Override
    public Long removeRangeForSortedSet(RedisKeyBuild redisKeyBuild, long start, long end) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        return this.redisTemplate.opsForZSet().removeRange(key, start, end);
    }

    /**
     * 根据指定的score值的范围来移除成员
     *
     * @param redisKeyBuild
     * @param min
     * @param max
     * @return
     */
    @Override
    public Long removeRangeByScoreForSortedSet(RedisKeyBuild redisKeyBuild, double min, double max) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        return this.redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    /**
     * 获取key和otherKey的并集并存储在destKey中
     *
     * @param redisKeyBuild
     * @param otherRedisKeyBuild
     * @param destRedisKeyBuild
     * @return
     */
    @Override
    public Long unionAndStoreForSortedSet(RedisKeyBuild redisKeyBuild, RedisKeyBuild otherRedisKeyBuild, RedisKeyBuild destRedisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotBlank(otherRedisKeyBuild);
        CacheUtil.checkNotBlank(destRedisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        String otherKey = otherRedisKeyBuild.getRealKey();
        String destKey = destRedisKeyBuild.getRealKey();
        return this.redisTemplate.opsForZSet().unionAndStore(key, otherKey, destKey);
    }

    /**
     * 获取key和otherKeys的并集并存储在destKey中
     *
     * @param redisKeyBuild
     * @param otherRedisKeyBuilds
     * @param destRedisKeyBuild
     * @return
     */
    @Override
    public Long unionAndStoreForSortedSet(RedisKeyBuild redisKeyBuild, Collection<RedisKeyBuild> otherRedisKeyBuilds, RedisKeyBuild destRedisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(otherRedisKeyBuilds);
        CacheUtil.checkNotBlank(destRedisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        List<String> otherKeys = CacheUtil.getBatchKey(otherRedisKeyBuilds);
        String destKey = destRedisKeyBuild.getRealKey();
        return this.redisTemplate.opsForZSet().unionAndStore(key, otherKeys, destKey);
    }

    /**
     * 获取key和otherKey的交集并存储在destKey中
     *
     * @param redisKeyBuild
     * @param otherRedisKeyBuild
     * @param destRedisKeyBuild
     * @return
     */
    @Override
    public Long intersectAndStoreForSortedSet(RedisKeyBuild redisKeyBuild, RedisKeyBuild otherRedisKeyBuild, RedisKeyBuild destRedisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotBlank(otherRedisKeyBuild);
        CacheUtil.checkNotBlank(destRedisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        String otherKey = otherRedisKeyBuild.getRealKey();
        String destKey = destRedisKeyBuild.getRealKey();
        return this.redisTemplate.opsForZSet().intersectAndStore(key, otherKey, destKey);
    }

    /**
     * 获取key和otherKeys的交集并存储在destKey中
     *
     * @param redisKeyBuild
     * @param otherRedisKeyBuilds
     * @param destRedisKeyBuild
     * @return
     */
    @Override
    public Long intersectAndStoreForSortedSet(RedisKeyBuild redisKeyBuild, Collection<RedisKeyBuild> otherRedisKeyBuilds, RedisKeyBuild destRedisKeyBuild) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        CacheUtil.checkNotEmpty(otherRedisKeyBuilds);
        CacheUtil.checkNotBlank(destRedisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        List<String> otherKeys = CacheUtil.getBatchKey(otherRedisKeyBuilds);
        String destKey = destRedisKeyBuild.getRealKey();
        return this.redisTemplate.opsForZSet().intersectAndStore(key, otherKeys, destKey);
    }

    /**
     * 游标遍历
     *
     * @param redisKeyBuild
     * @param options
     * @return
     */
    @Override
    public Cursor<ZSetOperations.TypedTuple<String>> scanForSortedSet(RedisKeyBuild redisKeyBuild, ScanOptions options) {
        CacheUtil.checkNotBlank(redisKeyBuild);
        String key = redisKeyBuild.getRealKey();
        return this.redisTemplate.opsForZSet().scan(key, options);
    }

    /**
     * 不对外使用
     *
     * @param redisKeyBuild
     * @param genericReturnType
     * @return
     */
    @Override
    public <T> T getByType(RedisKeyBuild redisKeyBuild, Type genericReturnType) {
        String key = redisKeyBuild.getRealKey();
        String s = this.redisTemplate.boundValueOps(key).get();
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        return JSONObject.parseObject(s ,genericReturnType);
    }

    /**
     * 获取实例
     *
     * @return
     */
    @Override
    public RedisTemplate getInstance() {
        return this.redisTemplate;
    }


    public <T> T getComplex(Object source, Class<T> clazz) {
        if(source == null){
            return null;
        }
        if(clazz.isAssignableFrom(String.class)){
            if(source instanceof String){
                return (T) source;
            }else{
                return (T) JSON.toJSONString(source);
            }
        }
        return source instanceof String ? JSON.parseObject((String) source, CacheUtil.buildType(clazz)) : null;
    }

    public <T> List<T> parseObjects(List<Object> sources, Class<T> clazz) {
        if(sources == null){
            return new ArrayList<>();
        }
        if(clazz.isAssignableFrom(String.class)){
            return (List<T>) sources.stream()
                    .filter(Objects::nonNull)
                    .map(each -> each instanceof String ? (String) each : JSON.toJSONString(each))
                    .toList();

        }
        return (List<T>) sources.stream()
                .filter(Objects::nonNull)
                .map(each -> each instanceof String ? JSON.parseObject((String) each, CacheUtil.buildType(clazz)) : null)
                .toList();
    }

    public <T> Set<T> parseObjects(Set<Object> sources, Class<T> clazz){
        if(sources == null){
            return new HashSet<>();
        }
        if(clazz.isAssignableFrom(String.class)){
            return (Set<T>) sources.stream()
                    .map(each -> each instanceof String ? (String) each : JSON.toJSONString(each))
                    .toList();
        }
        return (Set<T>) sources.stream()
                .map(each -> each instanceof String ? JSON.parseObject((String) each, CacheUtil.buildType(clazz)) : null)
                .toList();
    }

    public <T> Set<ZSetOperations.TypedTuple<T>> typedTupleStringParseObjects(Set<ZSetOperations.TypedTuple<String>> sources, Class<T> clazz){
        if (sources == null) {
            return new HashSet<>();
        }
        Set<ZSetOperations.TypedTuple<T>> set = new HashSet<>(sources.size());
        for (ZSetOperations.TypedTuple<String> typedTuple : sources) {
            String value = typedTuple.getValue();
            T complex = getComplex(value, clazz);
            Double score = typedTuple.getScore();
            DefaultTypedTuple defaultTypedTuple = new DefaultTypedTuple(complex,score);
            set.add(defaultTypedTuple);
        }
        return set;
    }

    public <T> Set<ZSetOperations.TypedTuple<T>> typedTupleParseObjects(Set<ZSetOperations.TypedTuple> sources, Class<T> clazz){
        if (sources == null) {
            return new HashSet<>();
        }
        Set<ZSetOperations.TypedTuple<T>> set = new HashSet<>(sources.size());
        for (ZSetOperations.TypedTuple typedTuple : sources) {
            Object value = typedTuple.getValue();
            T complex = getComplex(value, clazz);
            Double score = typedTuple.getScore();
            DefaultTypedTuple defaultTypedTuple = new DefaultTypedTuple(complex,score);
            set.add(defaultTypedTuple);
        }
        return set;
    }
}
