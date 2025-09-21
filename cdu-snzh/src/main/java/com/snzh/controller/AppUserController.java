package com.snzh.controller;

import com.snzh.domain.ResponseResult;
import com.snzh.domain.dto.*;
import com.snzh.domain.vo.PageVo;
import com.snzh.domain.vo.UserInfoVO;
import com.snzh.domain.vo.UserListVO;
import com.snzh.domain.vo.WxLoginVO;
import com.snzh.service.IAppUserService;
import com.snzh.threadlocal.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户接口", description = "用户相关接口")
public class AppUserController {

    private final IAppUserService userService;

    @PostMapping("/wx/login")
    @Operation(summary = "微信小程序登录", description = "通过微信小程序code登录")
    public ResponseResult<WxLoginVO> wxLogin(@Valid @RequestBody WxLoginDTO wxLoginDTO) {
        log.info("微信小程序登录: {}", wxLoginDTO.getCode());
        return ResponseResult.success(userService.wxLogin(wxLoginDTO));
    }

    @PutMapping("/info/update")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户信息")
    public ResponseResult<Boolean> updateUserInfo(@Valid @RequestBody UserInfoUpdateDTO userInfoUpdateDTO) {
        log.info("更新用户信息: {}", userInfoUpdateDTO);
        Long userId = Long.valueOf(UserContext.get("userId"));
        return ResponseResult.success(userService.updateUserInfo(userId, userInfoUpdateDTO));
    }

    @PostMapping("/wx/phone")
    @Operation(summary = "获取微信手机号", description = "获取用户微信绑定的手机号")
    public ResponseResult<Boolean> bindWxPhone(@Valid @RequestBody WxPhoneDTO wxPhoneDTO) {
        log.info("获取微信手机号: {}", wxPhoneDTO.getCode());
        Long userId = Long.valueOf(UserContext.get("userId"));
        return ResponseResult.success(userService.bindWxPhone(userId, wxPhoneDTO));
    }

    // ----------------------- 管理端 -------------------------------
    @GetMapping("/info/{userId}")
    @Operation(summary = "根据用户ID获取信息", description = "管理端获取用户详情")
    public ResponseResult<UserInfoVO> getUserInfoById(@PathVariable("userId") Long userId) {
        log.info("根据用户ID获取信息: {}", userId);
        return ResponseResult.success(userService.getUserInfo(userId));
    }

    @Operation(summary = "搜索用户列表", description = "管理端搜索用户列表")
    @PostMapping("/search")
    public ResponseResult<PageVo<UserListVO>> searchUserList(@RequestBody @Valid UserSearchDTO userSearchDTO){
        log.info("搜索用户列表 {}", userSearchDTO);
        return ResponseResult.success(userService.getUserOrSearch(userSearchDTO));
    }

    @Operation(summary = "获取用户列表", description = "管理端获取用户列表")
    @GetMapping("/list")
    public ResponseResult<PageVo<UserListVO>> getUserList() {
        log.info("获取用户列表");
        return ResponseResult.success(userService.getUserOrSearch(null));
    }

    @Operation(summary = "更新用户状态")
    @PostMapping("/update/status")
    public ResponseResult<Boolean> updateStatus(@RequestBody @Valid UpdateRoleStatusDTO updateRoleStatusDTO) {
        log.info("更新用户id为 {} 的状态为 {}", updateRoleStatusDTO.getId(),updateRoleStatusDTO.getStatus());
        return ResponseResult.success(userService.updateStatus(updateRoleStatusDTO.getId(), updateRoleStatusDTO.getStatus()));
    }

}
