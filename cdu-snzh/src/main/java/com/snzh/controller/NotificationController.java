package com.snzh.controller;

import com.snzh.annotation.RequireAdmin;
import com.snzh.domain.ResponseResult;
import com.snzh.service.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author haibara
 * @description 消息通知控制器
 * @since 2025/10/2
 */
@Slf4j
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
@Tag(name = "消息通知API", description = "消息通知相关接口")
public class NotificationController {

    private final INotificationService notificationService;

    @RequireAdmin
    @PostMapping("/admin/system-announcement")
    @Operation(summary = "【管理端】发送系统公告", description = "向指定用户发送系统公告通知")
    public ResponseResult<Boolean> sendSystemAnnouncement(
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "公告标题", required = true)
            @RequestParam String title,
            @Parameter(description = "公告内容", required = true)
            @RequestParam String content) {
        
        Boolean success = notificationService.sendSystemAnnouncement(userId, title, content);
        return ResponseResult.success(success);
    }

    @RequireAdmin
    @PostMapping("/admin/batch-announcement")
    @Operation(summary = "【管理端】批量发送系统公告", description = "向多个用户批量发送系统公告")
    public ResponseResult<String> batchSendSystemAnnouncement(
            @Parameter(description = "用户ID列表", required = true)
            @RequestBody java.util.List<Long> userIds,
            @Parameter(description = "公告标题", required = true)
            @RequestParam String title,
            @Parameter(description = "公告内容", required = true)
            @RequestParam String content) {
        
        int successCount = 0;
        int failCount = 0;

        for (Long userId : userIds) {
            try {
                boolean success = notificationService.sendSystemAnnouncement(userId, title, content);
                if (success) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                log.error("批量发送公告失败，用户ID：{}", userId, e);
                failCount++;
            }
        }

        String message = String.format("发送完成，成功%d条，失败%d条", successCount, failCount);
        return ResponseResult.success(message);
    }
}

