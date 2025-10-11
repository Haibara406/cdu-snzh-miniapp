package com.snzh.controller;

import com.snzh.annotation.RequireAdmin;
import com.snzh.constants.ValidationConstants;
import com.snzh.domain.ResponseResult;
import com.snzh.domain.dto.*;
import com.snzh.domain.vo.AdminUserVO;
import com.snzh.domain.vo.PageVo;
import com.snzh.service.IAdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author haibara
 * @description 管理员用户管理控制器
 * @since 2025/10/11
 */
@Slf4j
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
@Tag(name = "管理员用户管理", description = "管理员CRUD相关接口（仅超级管理员）")
public class AdminUserController {

    private final IAdminUserService adminUserService;

    @RequireAdmin(superAdminOnly = true)
    @PostMapping("/create")
    @Operation(summary = "创建管理员", description = "超级管理员创建新的管理员账号")
    public ResponseResult<Boolean> createAdmin(@Valid @RequestBody AdminUserCreateDTO dto) {
        log.info("创建管理员：username={}", dto.getUsername());
        return ResponseResult.success(adminUserService.createAdmin(dto));
    }

    @RequireAdmin(superAdminOnly = true)
    @PutMapping("/update")
    @Operation(summary = "更新管理员信息", description = "超级管理员更新管理员基本信息")
    public ResponseResult<Boolean> updateAdmin(@Valid @RequestBody AdminUserUpdateDTO dto) {
        log.info("更新管理员信息：adminId={}", dto.getId());
        return ResponseResult.success(adminUserService.updateAdmin(dto));
    }

    @RequireAdmin(superAdminOnly = true)
    @PostMapping("/update-password")
    @Operation(summary = "修改管理员密码", description = "超级管理员重置管理员密码")
    public ResponseResult<Boolean> updatePassword(@Valid @RequestBody AdminPasswordDTO dto) {
        log.info("修改管理员密码：adminId={}", dto.getAdminId());
        return ResponseResult.success(adminUserService.updatePassword(dto));
    }

    @RequireAdmin(superAdminOnly = true)
    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除管理员", description = "超级管理员批量删除管理员（修改状态为禁用）")
    public ResponseResult<Boolean> batchDelete(
            @Parameter(description = "管理员ID列表", required = true)
            @RequestBody @NotEmpty(message = ValidationConstants.ADMIN_IDS_NOT_EMPTY) List<Long> adminIds) {
        log.info("批量删除管理员：adminIds={}", adminIds);
        return ResponseResult.success(adminUserService.batchDelete(adminIds));
    }

    @RequireAdmin(superAdminOnly = true)
    @PostMapping("/update-status")
    @Operation(summary = "修改管理员状态", description = "超级管理员启用/禁用管理员")
    public ResponseResult<Boolean> updateStatus(
            @Parameter(description = "管理员ID", required = true) @RequestParam Long adminId,
            @Parameter(description = "状态：0-禁用，1-启用", required = true) @RequestParam Integer status) {
        log.info("修改管理员状态：adminId={}, status={}", adminId, status);
        return ResponseResult.success(adminUserService.updateStatus(adminId, status));
    }

    @RequireAdmin(superAdminOnly = true)
    @GetMapping("/list")
    @Operation(summary = "管理员列表", description = "分页查询管理员列表（仅超级管理员）")
    public ResponseResult<PageVo<AdminUserVO>> getAdminList(@Valid AdminUserQueryDTO dto) {
        log.info("查询管理员列表：{}", dto);
        return ResponseResult.success(adminUserService.getAdminList(dto));
    }

    @RequireAdmin(superAdminOnly = true)
    @GetMapping("/detail/{adminId}")
    @Operation(summary = "管理员详情", description = "根据ID获取管理员详细信息（仅超级管理员）")
    public ResponseResult<AdminUserVO> getAdminById(
            @Parameter(description = "管理员ID", required = true) @PathVariable Long adminId) {
        log.info("查询管理员详情：adminId={}", adminId);
        return ResponseResult.success(adminUserService.getAdminById(adminId));
    }
}

