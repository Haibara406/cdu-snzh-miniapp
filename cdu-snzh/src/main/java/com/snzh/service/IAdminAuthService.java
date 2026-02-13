package com.snzh.service;

import com.snzh.domain.dto.AdminLoginDTO;
import com.snzh.domain.vo.AdminInfoVO;
import com.snzh.domain.vo.AdminLoginVO;
import com.snzh.domain.vo.RefreshTokenVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author haibara
 * @description 管理员认证服务接口
 * @since 2025/10/11
 */
public interface IAdminAuthService {

    /**
     * 管理员登录
     */
    AdminLoginVO login(AdminLoginDTO loginDTO, HttpServletRequest request);

    /**
     * 管理员登出
     */
    Boolean logout(Long adminId);

    /**
     * 获取当前管理员信息
     */
    AdminInfoVO getCurrentAdminInfo(Long adminId);

    /**
     * 刷新访问令牌
     */
    RefreshTokenVO refreshToken(String refreshToken);
}

