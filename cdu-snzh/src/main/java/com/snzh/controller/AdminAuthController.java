package com.snzh.controller;

import com.snzh.annotation.RequireAdmin;
import com.snzh.domain.ResponseResult;
import com.snzh.domain.dto.AdminLoginDTO;
import com.snzh.domain.vo.AdminInfoVO;
import com.snzh.domain.vo.AdminLoginVO;
import com.snzh.service.IAdminAuthService;
import com.snzh.threadlocal.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author haibara
 * @description 管理端认证控制器
 * @since 2025/10/11
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "管理端认证接口", description = "管理员登录、登出、信息获取")
public class AdminAuthController {

    private final IAdminAuthService adminAuthService;

    @PostMapping("/login")
    @Operation(summary = "管理 员登录", description = "通过账号密码登录")
    public ResponseResult<AdminLoginVO> login(@Valid @RequestBody AdminLoginDTO loginDTO, HttpServletRequest request) {
        log.info("管理员登录请求: username={}", loginDTO.getUsername());
        return ResponseResult.success(adminAuthService.login(loginDTO, request));
    }

    @PostMapping("/logout")
    @RequireAdmin
    @Operation(summary = "管理员登出", description = "退出登录")
    public ResponseResult<Void> logout() {
        Long adminId = Long.valueOf(UserContext.get("userId"));
        log.info("管理员登出: adminId={}", adminId);
        adminAuthService.logout(adminId);
        return ResponseResult.success();
    }

    @GetMapping("/info")
    @RequireAdmin
    @Operation(summary = "获取当前管理员信息", description = "获取当前登录管理员的详细信息")
    public ResponseResult<AdminInfoVO> getCurrentInfo() {
        Long adminId = Long.valueOf(UserContext.get("userId"));
        log.info("获取管理员信息: adminId={}", adminId);
        return ResponseResult.success(adminAuthService.getCurrentAdminInfo(adminId));
    }
}

