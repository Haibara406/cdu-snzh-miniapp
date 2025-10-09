package com.snzh.init;

import com.snzh.domain.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author haibara
 * @description 缓存管理控制器 - 用于手动触发预热和监控缓存状态
 * @since 2025/10/9
 */
@Slf4j
@Tag(name = "系统缓存管理", description = "缓存预热与监控相关接口")
@RestController
@RequestMapping("/admin/cache")
@RequiredArgsConstructor
public class CacheManageController {

    private final IInitService initService;

    @Operation(summary = "获取缓存状态", description = "查看当前系统各项缓存的状态信息")
    @GetMapping("/status")
    public ResponseResult<Map<String, Object>> getCacheStatus() {
        try {
            Map<String, Object> status = initService.getCacheStatus();
            return ResponseResult.success(status);
        } catch (Exception e) {
            log.error("获取缓存状态失败", e);
            return ResponseResult.failure("获取缓存状态失败：" + e.getMessage());
        }
    }

    @Operation(summary = "重新预热所有缓存", description = "执行所有缓存预热任务，适用于系统维护后的缓存重建")
    @PostMapping("/reload-all")
    public ResponseResult<String> reloadAllCache() {
        try {
            log.info("手动触发：重新预热所有缓存");
            long startTime = System.currentTimeMillis();
            initService.executeAllInitTasks();
            long endTime = System.currentTimeMillis();
            String message = String.format("所有缓存预热完成，耗时: %dms", endTime - startTime);
            return ResponseResult.success(message);
        } catch (Exception e) {
            log.error("重新预热所有缓存失败", e);
            return ResponseResult.failure("缓存预热失败：" + e.getMessage());
        }
    }

    @Operation(summary = "预热基础设施类型缓存", description = "单独预热基础设施类型相关缓存")
    @PostMapping("/reload-facility-types")
    public ResponseResult<String> reloadFacilityTypes() {
        try {
            log.info("手动触发：预热基础设施类型缓存");
            long startTime = System.currentTimeMillis();
            initService.preloadFacilityTypes();
            long endTime = System.currentTimeMillis();
            String message = String.format("基础设施类型缓存预热完成，耗时: %dms", endTime - startTime);
            return ResponseResult.success(message);
        } catch (Exception e) {
            log.error("预热基础设施类型缓存失败", e);
            return ResponseResult.failure("缓存预热失败：" + e.getMessage());
        }
    }

    @Operation(summary = "预热基础设施缓存", description = "单独预热基础设施相关缓存")
    @PostMapping("/reload-facilities")
    public ResponseResult<String> reloadFacilities() {
        try {
            log.info("手动触发：预热基础设施缓存");
            long startTime = System.currentTimeMillis();
            initService.preloadFacilities();
            long endTime = System.currentTimeMillis();
            String message = String.format("基础设施缓存预热完成，耗时: %dms", endTime - startTime);
            return ResponseResult.success(message);
        } catch (Exception e) {
            log.error("预热基础设施缓存失败", e);
            return ResponseResult.failure("缓存预热失败：" + e.getMessage());
        }
    }

    @Operation(summary = "预热景点缓存", description = "预热景点列表和详情缓存")
    @PostMapping("/reload-scenic-spots")
    public ResponseResult<String> reloadScenicSpots() {
        try {
            log.info("手动触发：预热景点缓存");
            long startTime = System.currentTimeMillis();
            initService.preloadScenicSpots();
            initService.preloadScenicSpotDetails();
            long endTime = System.currentTimeMillis();
            String message = String.format("景点缓存预热完成，耗时: %dms", endTime - startTime);
            return ResponseResult.success(message);
        } catch (Exception e) {
            log.error("预热景点缓存失败", e);
            return ResponseResult.failure("缓存预热失败：" + e.getMessage());
        }
    }

    @Operation(summary = "预热景点门票缓存", description = "预热景点门票相关缓存")
    @PostMapping("/reload-tickets")
    public ResponseResult<String> reloadTickets() {
        try {
            log.info("手动触发：预热景点门票缓存");
            long startTime = System.currentTimeMillis();
            initService.preloadScenicTickets();
            long endTime = System.currentTimeMillis();
            String message = String.format("景点门票缓存预热完成，耗时: %dms", endTime - startTime);
            return ResponseResult.success(message);
        } catch (Exception e) {
            log.error("预热景点门票缓存失败", e);
            return ResponseResult.failure("缓存预热失败：" + e.getMessage());
        }
    }

}

