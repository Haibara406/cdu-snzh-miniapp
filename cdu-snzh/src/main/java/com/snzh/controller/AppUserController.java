package com.snzh.controller;

import com.snzh.domain.ResponseResult;
import com.snzh.domain.dto.*;
import com.snzh.domain.vo.UserInfoVO;
import com.snzh.domain.vo.UserListVO;
import com.snzh.domain.vo.WxLoginVO;
import com.snzh.service.IAppUserService;
import com.snzh.threadlocal.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户接口", description = "用户相关接口")
public class AppUserController {

    private final IAppUserService userService;

    @PostMapping("/wx/login")
    @Operation(summary = "微信小程序登录", description = "通过微信小程序code登录")
    public ResponseResult<WxLoginVO> wxLogin(@Valid @RequestBody WxLoginDTO wxLoginDTO) {
        log.info("微信小程序登录: {}", wxLoginDTO.getCode());
        WxLoginVO loginVO = userService.wxLogin(wxLoginDTO);
        return ResponseResult.success(loginVO);
    }

    @PutMapping("/info/update")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户信息")
    public ResponseResult<Boolean> updateUserInfo(@Valid @RequestBody UserInfoUpdateDTO userInfoUpdateDTO) {
        log.info("更新用户信息: {}", userInfoUpdateDTO);
        Long userId = Long.valueOf(UserContext.get("userId"));
        boolean result = userService.updateUserInfo(userId, userInfoUpdateDTO);
        return ResponseResult.success(result);
    }

    @PostMapping("/wx/phone")
    @Operation(summary = "获取微信手机号", description = "获取用户微信绑定的手机号")
    public ResponseResult<Boolean> bindWxPhone(@Valid @RequestBody WxPhoneDTO wxPhoneDTO) {
        log.info("获取微信手机号");
        Long userId = Long.valueOf(UserContext.get("userId"));
        boolean result = userService.bindWxPhone(userId, wxPhoneDTO);
        return ResponseResult.success(result);
    }

    // ----------------------- 管理端 -------------------------------
    @GetMapping("/info/{userId}")
    @Operation(summary = "根据用户ID获取信息", description = "管理端获取用户详情")
    public ResponseResult<UserInfoVO> getUserInfoById(@PathVariable("userId") Long userId) {
        log.info("根据用户ID获取信息: {}", userId);
        UserInfoVO userInfoVO = userService.getUserInfo(userId);
        return ResponseResult.success(userInfoVO);
    }

    @Operation(summary = "搜索用户列表", description = "管理端搜索用户列表")
    @PostMapping("/search")
    public ResponseResult<List<UserListVO>> searchUserList(@RequestBody @Valid UserSearchDTO userSearchDTO){
        return ResponseResult.success(userService.getUserOrSearch(userSearchDTO));
    }

    @Operation(summary = "获取用户列表", description = "管理端获取用户列表")
    @GetMapping("/list")
    public ResponseResult<List<UserListVO>> getUserList() {
        return ResponseResult.success(userService.getUserOrSearch(null));
    }

    @Operation(summary = "更新用户状态")
    @PostMapping("/update/status")
    public ResponseResult<Void> updateStatus(@RequestBody @Valid UpdateRoleStatusDTO updateRoleStatusDTO) {
        return userService.updateStatus(updateRoleStatusDTO.getId(), updateRoleStatusDTO.getStatus());
    }

}
