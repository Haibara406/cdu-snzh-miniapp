package com.snzh.init;

import java.util.Map;

/**
 * @author haibara
 * @description 系统初始化服务接口
 * @since 2025/10/9
 */
public interface IInitService {

    /**
     * 预热基础设施类型缓存
     * 加载所有启用的基础设施类型到Redis
     * 对应缓存键：FACILITY_TYPE_ALL, FACILITY_TYPE_SINGLE
     */
    void preloadFacilityTypes();

    /**
     * 预热基础设施缓存
     * 加载所有启用的基础设施及其按类型分组的缓存
     * 对应缓存键：FACILITY_FOR_TYPE, FACILITY_DETAILS
     */
    void preloadFacilities();

    /**
     * 预热景点列表缓存
     * 加载所有启用的景点信息到Redis
     * 对应缓存键：SCENIC_SPOT_LIST
     */
    void preloadScenicSpots();

    /**
     * 预热景点详情缓存
     * 加载所有景点的详细信息到Redis
     * 对应缓存键：SCENIC_SPOT_DETAIL
     */
    void preloadScenicSpotDetails();

    /**
     * 预热景点门票缓存
     * 加载所有景点的门票信息到Redis
     * 对应缓存键：SCENIC_TICKET_FOR_SPOT, SCENIC_TICKET_DETAIL
     */
    void preloadScenicTickets();

    /**
     * 执行所有初始化任务
     * 按顺序执行所有预热任务
     */
    void executeAllInitTasks();

    /**
     * 获取缓存状态信息
     * 返回各个缓存的存在状态和过期时间
     *
     * @return 缓存状态信息Map
     */
    Map<String, Object> getCacheStatus();
}

