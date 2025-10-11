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
    ADMIN_LOGIN("admin:login:%s","管理员id" ,"value为refreshToken"),
    FACILITY_TYPE_SINGLE("facility:type:%s", "基础设施类型id", "value为FacilityType类型"),
    FACILITY_TYPE_ALL("facility:type:all", "", "value为List<FacilityTypeVO>类型"),
    FACILITY_FOR_TYPE("facility:%s", "基础设施类型id", "value为List<FacilityVO>类型"),
    FACILITY_DETAILS("facility:details:%s", "基础设施id", "value为Facility类型"),
    SCENIC_TICKET_FOR_SPOT("scenic:ticket:spot:%s", "景点ID", "value为List<ScenicTicketVO>类型"),
    SCENIC_TICKET_DETAIL("scenic:ticket:detail:%s", "票ID", "value为ScenicTicketVO类型"),
    SCENIC_SPOT_DETAIL("scenic:spot:detail:%s", "景点id", "value为ScenicSpotVO类型"),
    SCENIC_SPOT_LIST("scenic:spot:list", "", "value为List<ScenicSpotVO>类型"),
    ORDER_DETAIL("order:detail:%s", "订单号", "value为OrderVO类型"),
    
    // AI智能客服相关
    AI_CHAT_MEMORY("ai:chat:memory:%s", "会话ID", "value为List<AiChatMessage>类型，存储会话历史"),
    AI_CHAT_CONTEXT("ai:chat:context:%s", "会话ID", "value为Hash类型，存储会话上下文变量")
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
