package com.snzh.init.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snzh.domain.entity.Facility;
import com.snzh.domain.entity.FacilityType;
import com.snzh.domain.entity.ScenicSpot;
import com.snzh.domain.entity.ScenicTicket;
import com.snzh.domain.vo.FacilityTypeVO;
import com.snzh.domain.vo.FacilityVO;
import com.snzh.domain.vo.ScenicSpotVO;
import com.snzh.domain.vo.ScenicTicketVO;
import com.snzh.enums.RedisKeyManage;
import com.snzh.enums.SpotTypeEnum;
import com.snzh.enums.StatusEnum;
import com.snzh.init.service.IInitService;
import com.snzh.mapper.FacilityMapper;
import com.snzh.mapper.FacilityTypeMapper;
import com.snzh.mapper.ScenicSpotMapper;
import com.snzh.mapper.ScenicTicketMapper;
import com.snzh.redis.RedisCache;
import com.snzh.redis.RedisKeyBuild;
import com.snzh.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author haibara
 * @description 系统初始化服务实现类
 * @since 2025/10/9
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InitServiceImpl implements IInitService {

    private final RedisCache redisCache;
    private final FacilityTypeMapper facilityTypeMapper;
    private final FacilityMapper facilityMapper;
    private final ScenicSpotMapper scenicSpotMapper;
    private final ScenicTicketMapper scenicTicketMapper;

    @Override
    public void preloadFacilityTypes() {
        log.info("--------开始预热基础设施类型缓存--------");
        try {
            long startTime = System.currentTimeMillis();

            // 查询所有启用的基础设施类型
            LambdaQueryWrapper<FacilityType> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FacilityType::getStatus, StatusEnum.RUN.getCode())
                    .orderByAsc(FacilityType::getId);
            List<FacilityType> facilityTypes = facilityTypeMapper.selectList(wrapper);

            if (facilityTypes != null && !facilityTypes.isEmpty()) {
                // 转换为VO
                List<FacilityTypeVO> facilityTypeVOList = facilityTypes.stream()
                        .map(facilityType -> BeanUtil.copyProperties(facilityType, FacilityTypeVO.class))
                        .toList();

                // 缓存类型列表（24小时过期）
                redisCache.set(
                        RedisKeyBuild.createKey(RedisKeyManage.FACILITY_TYPE_ALL),
                        facilityTypeVOList,
                        DateUtils.DAY_SECONDS,
                        TimeUnit.SECONDS
                );

                // 缓存每个类型的详情
                for (FacilityType facilityType : facilityTypes) {
                    redisCache.set(
                            RedisKeyBuild.createKey(RedisKeyManage.FACILITY_TYPE_SINGLE, facilityType.getId()),
                            facilityType,
                            DateUtils.DAY_SECONDS,
                            TimeUnit.SECONDS
                    );
                }

                long endTime = System.currentTimeMillis();
                log.info("--------基础设施类型缓存预热完成，类型列表: 1个，单个类型: {}个，耗时: {}ms--------",
                        facilityTypes.size(), endTime - startTime);
            } else {
                log.info("--------没有基础设施类型数据，跳过预热--------");
            }
        } catch (Exception e) {
            log.error("--------基础设施类型缓存预热失败--------", e);
        }
    }

    @Override
    public void preloadFacilities() {
        log.info("--------开始预热基础设施缓存--------");
        try {
            long startTime = System.currentTimeMillis();

            // 查询所有启用的基础设施
            LambdaQueryWrapper<Facility> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Facility::getStatus, StatusEnum.RUN.getCode())
                    .orderByAsc(Facility::getFacilityTypeId);
            List<Facility> facilities = facilityMapper.selectList(wrapper);

            if (facilities != null && !facilities.isEmpty()) {
                int detailsCached = 0;
                int typeGroupCached = 0;

                // 按类型分组
                Map<Long, List<Facility>> groupedByType = facilities.stream()
                        .collect(Collectors.groupingBy(Facility::getFacilityTypeId));

                // 缓存每个类型下的设施列表
                for (Map.Entry<Long, List<Facility>> entry : groupedByType.entrySet()) {
                    Long typeId = entry.getKey();
                    List<FacilityVO> facilityVOList = entry.getValue().stream()
                            .map(facility -> BeanUtil.copyProperties(facility, FacilityVO.class))
                            .toList();

                    redisCache.set(
                            RedisKeyBuild.createKey(RedisKeyManage.FACILITY_FOR_TYPE, typeId),
                            facilityVOList,
                            DateUtils.DAY_SECONDS,
                            TimeUnit.SECONDS
                    );
                    typeGroupCached++;
                }

                // 缓存每个设施的详情
                for (Facility facility : facilities) {
                    redisCache.set(
                            RedisKeyBuild.createKey(RedisKeyManage.FACILITY_DETAILS, facility.getId()),
                            facility,
                            DateUtils.DAY_SECONDS,
                            TimeUnit.SECONDS
                    );
                    detailsCached++;
                }

                long endTime = System.currentTimeMillis();
                log.info("--------基础设施缓存预热完成，类型分组: {}个，设施详情: {}个，耗时: {}ms--------",
                        typeGroupCached, detailsCached, endTime - startTime);
            } else {
                log.info("--------没有基础设施数据，跳过预热--------");
            }
        } catch (Exception e) {
            log.error("--------基础设施缓存预热失败--------", e);
        }
    }

    @Override
    public void preloadScenicSpots() {
        log.info("--------开始预热景点列表缓存--------");
        try {
            long startTime = System.currentTimeMillis();

            // 查询所有启用的景点
            LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ScenicSpot::getStatus, StatusEnum.RUN.getCode())
                    .eq(ScenicSpot::getSpotType, SpotTypeEnum.SCENIC_SPOT.getCode())
                    .eq(ScenicSpot::getParentId, 1)
                    .orderByAsc(ScenicSpot::getDisplayOrder);
            List<ScenicSpot> scenicSpots = scenicSpotMapper.selectList(wrapper);

            if (scenicSpots != null && !scenicSpots.isEmpty()) {
                // 转换为VO
                List<ScenicSpotVO> scenicSpotVOList = scenicSpots.stream()
                        .map(scenicSpot -> BeanUtil.copyProperties(scenicSpot, ScenicSpotVO.class))
                        .toList();

                // 缓存到Redis（24小时过期）
                redisCache.set(
                        RedisKeyBuild.createKey(RedisKeyManage.SCENIC_SPOT_LIST),
                        scenicSpotVOList,
                        DateUtils.DAY_SECONDS,
                        TimeUnit.SECONDS
                );

                long endTime = System.currentTimeMillis();
                log.info("--------景点列表缓存预热完成，缓存数量: {}，耗时: {}ms--------",
                        scenicSpots.size(), endTime - startTime);
            } else {
                log.info("--------没有景点数据，跳过预热--------");
            }
        } catch (Exception e) {
            log.error("--------景点列表缓存预热失败--------", e);
        }
    }

    @Override
    public void preloadScenicSpotDetails() {
        log.info("--------开始预热景点详情缓存--------");
        try {
            long startTime = System.currentTimeMillis();

            // 查询所有启用的景点
            LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ScenicSpot::getStatus, StatusEnum.RUN.getCode());
            List<ScenicSpot> scenicSpots = scenicSpotMapper.selectList(wrapper);

            if (scenicSpots != null && !scenicSpots.isEmpty()) {
                int cachedCount = 0;
                // 缓存每个景点的详情
                for (ScenicSpot scenicSpot : scenicSpots) {
                    ScenicSpotVO scenicSpotVO = BeanUtil.copyProperties(scenicSpot, ScenicSpotVO.class);
                    redisCache.set(
                            RedisKeyBuild.createKey(RedisKeyManage.SCENIC_SPOT_DETAIL, scenicSpot.getId()),
                            scenicSpotVO,
                            DateUtils.DAY_SECONDS,
                            TimeUnit.SECONDS
                    );
                    cachedCount++;
                }

                long endTime = System.currentTimeMillis();
                log.info("--------景点详情缓存预热完成，缓存数量: {}，耗时: {}ms--------",
                        cachedCount, endTime - startTime);
            } else {
                log.info("--------没有景点数据，跳过预热--------");
            }
        } catch (Exception e) {
            log.error("--------景点详情缓存预热失败--------", e);
        }
    }


    @Override
    public void preloadScenicTickets() {
        log.info("--------开始预热景点门票缓存--------");
        try {
            long startTime = System.currentTimeMillis();

            // 查询所有启用的景点门票
            LambdaQueryWrapper<ScenicTicket> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ScenicTicket::getStatus, StatusEnum.RUN.getCode())
                    .orderByAsc(ScenicTicket::getScenicSpotId);
            List<ScenicTicket> scenicTickets = scenicTicketMapper.selectList(wrapper);

            if (scenicTickets != null && !scenicTickets.isEmpty()) {
                int ticketDetailsCached = 0;
                int spotTicketsCached = 0;

                // 按景点分组
                Map<Long, List<ScenicTicket>> groupedBySpot = scenicTickets.stream()
                        .collect(Collectors.groupingBy(ScenicTicket::getScenicSpotId));

                // 缓存每个景点的门票列表
                for (Map.Entry<Long, List<ScenicTicket>> entry : groupedBySpot.entrySet()) {
                    Long spotId = entry.getKey();
                    List<ScenicTicketVO> ticketVOList = entry.getValue().stream()
                            .map(ticket -> BeanUtil.copyProperties(ticket, ScenicTicketVO.class))
                            .toList();

                    redisCache.set(
                            RedisKeyBuild.createKey(RedisKeyManage.SCENIC_TICKET_FOR_SPOT, spotId),
                            ticketVOList,
                            DateUtils.DAY_SECONDS,
                            TimeUnit.SECONDS
                    );
                    spotTicketsCached++;
                }

                // 缓存每个门票的详情
                for (ScenicTicket ticket : scenicTickets) {
                    ScenicTicketVO ticketVO = BeanUtil.copyProperties(ticket, ScenicTicketVO.class);
                    redisCache.set(
                            RedisKeyBuild.createKey(RedisKeyManage.SCENIC_TICKET_DETAIL, ticket.getId()),
                            ticketVO,
                            DateUtils.DAY_SECONDS,
                            TimeUnit.SECONDS
                    );
                    ticketDetailsCached++;
                }

                long endTime = System.currentTimeMillis();
                log.info("--------景点门票缓存预热完成，景点门票列表: {}个，门票详情: {}个，耗时: {}ms--------",
                        spotTicketsCached, ticketDetailsCached, endTime - startTime);
            } else {
                log.info("--------没有景点门票数据，跳过预热--------");
            }
        } catch (Exception e) {
            log.error("--------景点门票缓存预热失败--------", e);
        }
    }


    @Override
    public void executeAllInitTasks() {
        log.info("--------开始执行系统初始化任务--------");
        long totalStartTime = System.currentTimeMillis();

        try {
            // 1. 预热基础设施类型
            preloadFacilityTypes();

            // 2. 预热基础设施
            preloadFacilities();

            // 3. 预热景点列表
            preloadScenicSpots();

            // 4. 预热景点详情
            preloadScenicSpotDetails();

            // 5. 预热景点门票
            preloadScenicTickets();

            long totalEndTime = System.currentTimeMillis();
            log.info("--------系统初始化任务执行完成，总耗时: {}ms--------", totalEndTime - totalStartTime);
        } catch (Exception e) {
            log.error("--------系统初始化任务执行失败--------", e);
        }
    }

    @Override
    public Map<String, Object> getCacheStatus() {
        Map<String, Object> status = new LinkedHashMap<>();

        try {
            // 检查基础设施类型缓存
            addCacheStatus(status, "facilityTypeAll", RedisKeyManage.FACILITY_TYPE_ALL);

            // 检查景点列表缓存
            addCacheStatus(status, "scenicSpotList", RedisKeyManage.SCENIC_SPOT_LIST);

            // 统计基础设施详情缓存数量
            Set<String> facilityKeys = redisCache.keys("facility:details:*");
            status.put("facilityDetailsCount", facilityKeys != null ? facilityKeys.size() : 0);

            // 统计景点详情缓存数量
            Set<String> spotKeys = redisCache.keys("scenic:spot:detail:*");
            status.put("scenicSpotDetailsCount", spotKeys != null ? spotKeys.size() : 0);

            // 统计门票缓存数量
            Set<String> ticketKeys = redisCache.keys("scenic:ticket:*");
            status.put("scenicTicketsCount", ticketKeys != null ? ticketKeys.size() : 0);

            // 添加总体状态
            long totalCacheKeys = 0;
            if (facilityKeys != null) {
                totalCacheKeys += facilityKeys.size();
            }
            if (spotKeys != null) {
                totalCacheKeys += spotKeys.size();
            }
            if (ticketKeys != null) {
                totalCacheKeys += ticketKeys.size();
            }
            status.put("totalCacheKeys", totalCacheKeys);

        } catch (Exception e) {
            log.error("--------获取缓存状态失败--------", e);
            status.put("error", e.getMessage());
        }

        return status;
    }

    /**
     * 添加单个缓存的状态信息
     */
    private void addCacheStatus(Map<String, Object> status, String name, RedisKeyManage keyManage, Object... args) {
        try {
            RedisKeyBuild key = RedisKeyBuild.createKey(keyManage, args);
            Boolean hasKey = redisCache.hasKey(key);
            Long expireSeconds = hasKey ? redisCache.getExpire(key) : null;

            Map<String, Object> cacheInfo = new HashMap<>();
            cacheInfo.put("exists", hasKey != null && hasKey);
            cacheInfo.put("expireSeconds", expireSeconds);
            if (expireSeconds != null && expireSeconds > 0) {
                cacheInfo.put("expireHours", String.format("%.2f", expireSeconds / 3600.0));
            }

            status.put(name, cacheInfo);
        } catch (Exception e) {
            log.error("--------获取缓存状态失败: {}--------", name, e);
            status.put(name, Collections.singletonMap("error", e.getMessage()));
        }
    }
}
