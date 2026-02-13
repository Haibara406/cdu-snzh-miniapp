package com.snzh.controller;

import com.snzh.annotation.RequireAdmin;
import com.snzh.domain.ResponseResult;
import com.snzh.domain.dto.CheckinHistoryQueryDTO;
import com.snzh.domain.dto.CheckinRequestDTO;
import com.snzh.domain.vo.BadgeCollectionVO;
import com.snzh.domain.vo.CheckinHistoryVO;
import com.snzh.domain.vo.CheckinResponseVO;
import com.snzh.domain.vo.PageVo;
import com.snzh.service.IScenicCheckinService;
import com.snzh.threadlocal.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author haibara
 * @description 景区签到控制器
 * @since 2025/02/13
 */
@Slf4j
@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
@Tag(name = "景区签到接口", description = "景区打卡签到相关接口")
public class ScenicCheckinController {

    private final IScenicCheckinService scenicCheckinService;

    // ==================== 用户端接口 ====================

    @PostMapping("/spot")
    @Operation(summary = "景点签到", description = "用户在景点打卡签到")
    public ResponseResult<CheckinResponseVO> checkin(@Valid @RequestBody CheckinRequestDTO checkinRequestDTO) {
        Long userId = Long.valueOf(UserContext.get("userId"));
        log.info("用户 {} 发起签到请求", userId);
        CheckinResponseVO response = scenicCheckinService.checkin(userId, checkinRequestDTO);
        return ResponseResult.success(response);
    }

    @PostMapping("/history")
    @Operation(summary = "查询签到历史", description = "查询用户的签到历史记录")
    public ResponseResult<PageVo<CheckinHistoryVO>> getCheckinHistory(
            @RequestBody @Valid CheckinHistoryQueryDTO queryDTO) {
        Long userId = Long.valueOf(UserContext.get("userId"));
        log.info("用户 {} 查询签到历史", userId);
        PageVo<CheckinHistoryVO> history = scenicCheckinService.getCheckinHistory(userId, queryDTO);
        return ResponseResult.success(history);
    }

    @GetMapping("/badges")
    @Operation(summary = "查询勋章收藏", description = "查询用户收集的所有景点勋章")
    public ResponseResult<List<BadgeCollectionVO>> getBadgeCollection() {
        Long userId = Long.valueOf(UserContext.get("userId"));
        log.info("用户 {} 查询勋章收藏", userId);
        List<BadgeCollectionVO> badges = scenicCheckinService.getBadgeCollection(userId);
        return ResponseResult.success(badges);
    }

    // ==================== 管理端接口 ====================

    @RequireAdmin
    @PostMapping("/admin/history")
    @Operation(summary = "管理端查询签到历史", description = "管理端分页查询所有用户的签到历史")
    public ResponseResult<PageVo<CheckinHistoryVO>> getCheckinHistoryForAdmin(
            @RequestBody @Valid CheckinHistoryQueryDTO queryDTO) {
        log.info("管理端查询签到历史，查询条件: {}", queryDTO);
        PageVo<CheckinHistoryVO> history = scenicCheckinService.getCheckinHistoryForAdmin(queryDTO);
        return ResponseResult.success(history);
    }

    @RequireAdmin
    @GetMapping("/admin/badges/{userId}")
    @Operation(summary = "管理端查询用户勋章", description = "管理端查询指定用户的勋章收藏")
    public ResponseResult<List<BadgeCollectionVO>> getBadgeCollectionForAdmin(@PathVariable Long userId) {
        log.info("管理端查询用户 {} 的勋章收藏", userId);
        List<BadgeCollectionVO> badges = scenicCheckinService.getBadgeCollection(userId);
        return ResponseResult.success(badges);
    }
}
