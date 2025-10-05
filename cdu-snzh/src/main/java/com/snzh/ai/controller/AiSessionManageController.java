package com.snzh.ai.controller;

import com.snzh.ai.task.AiSessionCleanupTask;
import com.snzh.domain.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author haibara
 * @description AI会话管理控制器（管理端）
 * @since 2025/10/5
 */
@Slf4j
@RestController
@RequestMapping("/ai/session/manage")
@RequiredArgsConstructor
@Tag(name = "AI会话管理", description = "AI会话管理接口（管理端）")
public class AiSessionManageController {

    private final AiSessionCleanupTask sessionCleanupTask;

    /**
     * 手动清理旧会话
     */
    @PostMapping("/cleanup")
    @Operation(summary = "手动清理旧会话", description = "清理两周前未更新的会话")
    public ResponseResult<Integer> manualCleanup() {
        log.info("管理端手动触发清理旧会话");
        int count = sessionCleanupTask.manualCleanup();
        return ResponseResult.success(count);
    }
}

