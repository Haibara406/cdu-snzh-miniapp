package com.snzh.init.task;

import com.snzh.init.service.IInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author haibara
 * @description 缓存刷新定时任务
 * @since 2025/10/9
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheRefreshTask {

    private final IInitService initService;

    /**
     * 每天凌晨1点执行全量缓存刷新
     * 避开业务高峰期，保证缓存数据的新鲜度
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void refreshAllCacheDaily() {
        log.info("========================================================");
        log.info("         定时任务：开始执行每日缓存全量刷新              ");
        log.info("========================================================");
        
        try {
            long startTime = System.currentTimeMillis();
            initService.executeAllInitTasks();
            long endTime = System.currentTimeMillis();
            
            log.info("========================================================");
            log.info("    定时任务：每日缓存全量刷新成功，耗时: {}ms         ", endTime - startTime);
            log.info("========================================================");
        } catch (Exception e) {
            log.error("========================================================");
            log.error("         定时任务：每日缓存全量刷新失败                 ");
            log.error("========================================================", e);
        }
    }

    /**
     * 每8小时刷新一次基础设施相关缓存
     * 基础设施数据变更相对频繁，需要更频繁的刷新
     */
    @Scheduled(cron = "0 0 */8 * * ?")
    public void refreshFacilityCacheEveryEightHours() {
        log.info("--------定时任务：开始刷新基础设施缓存（每2小时）--------");
        
        try {
            long startTime = System.currentTimeMillis();
            initService.preloadFacilityTypes();
            initService.preloadFacilities();
            long endTime = System.currentTimeMillis();
            
            log.info("--------定时任务：基础设施缓存刷新成功，耗时: {}ms--------", endTime - startTime);
        } catch (Exception e) {
            log.error("--------定时任务：基础设施缓存刷新失败--------", e);
        }
    }

    /**
     * 每8小时刷新一次景点相关缓存
     * 景点数据相对稳定，8小时刷新一次即可
     */
    @Scheduled(cron = "0 0 */8 * * ?")
    public void refreshScenicSpotCacheEveryEightHours() {
        log.info("--------定时任务：开始刷新景点缓存（每4小时）--------");
        
        try {
            long startTime = System.currentTimeMillis();
            initService.preloadScenicSpots();
            initService.preloadScenicSpotDetails();
            long endTime = System.currentTimeMillis();
            
            log.info("--------定时任务：景点缓存刷新成功，耗时: {}ms--------", endTime - startTime);
        } catch (Exception e) {
            log.error("--------定时任务：景点缓存刷新失败--------", e);
        }
    }

    /**
     * 每6小时刷新一次景点门票缓存
     * 门票数据较为稳定，6小时刷新一次
     */
    @Scheduled(cron = "0 0 */6 * * ?")
    public void refreshTicketCacheEverySixHours() {
        log.info("--------定时任务：开始刷新景点门票缓存（每6小时）--------");
        
        try {
            long startTime = System.currentTimeMillis();
            initService.preloadScenicTickets();
            long endTime = System.currentTimeMillis();
            
            log.info("--------定时任务：景点门票缓存刷新成功，耗时: {}ms--------", endTime - startTime);
        } catch (Exception e) {
            log.error("--------定时任务：景点门票缓存刷新失败--------", e);
        }
    }

}

