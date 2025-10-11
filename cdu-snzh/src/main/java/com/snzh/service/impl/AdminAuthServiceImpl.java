package com.snzh.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.snzh.constants.ErrorConst;
import com.snzh.domain.dto.AdminLoginDTO;
import com.snzh.domain.entity.AdminUser;
import com.snzh.domain.properties.JwtProperties;
import com.snzh.domain.vo.AdminInfoVO;
import com.snzh.domain.vo.AdminLoginVO;
import com.snzh.enums.RedisKeyManage;
import com.snzh.enums.StatusEnum;
import com.snzh.exceptions.AccountLockedException;
import com.snzh.exceptions.AccountNotFoundException;
import com.snzh.exceptions.AdminLoginException;
import com.snzh.mapper.AdminUserMapper;
import com.snzh.redis.RedisCache;
import com.snzh.redis.RedisKeyBuild;
import com.snzh.service.IAdminAuthService;
import com.snzh.utils.IpUtils;
import com.snzh.utils.JwtUtil;
import com.snzh.utils.PasswordUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author haibara
 * @description 管理员认证服务实现类
 * @since 2025/10/11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements IAdminAuthService {

    private final AdminUserMapper adminUserMapper;
    private final RedisCache redisCache;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminLoginVO login(AdminLoginDTO loginDTO, HttpServletRequest request) {
        // 1. 查询管理员账号
        AdminUser adminUser = adminUserMapper.selectOne(
                Wrappers.lambdaQuery(AdminUser.class)
                        .eq(AdminUser::getUsername, loginDTO.getUsername())
        );

        if (adminUser == null) {
            throw new AccountNotFoundException(ErrorConst.ADMIN_LOGIN_ERROR);
        }

        // 2. 验证密码
        if (!PasswordUtils.matches(loginDTO.getPassword(), adminUser.getPassword())) {
            throw new AdminLoginException(ErrorConst.ADMIN_LOGIN_ERROR);
        }

        // 3. 检查账号状态
        if (StatusEnum.STOP.getCode().equals(adminUser.getStatus())) {
            throw new AccountLockedException(ErrorConst.ADMIN_ACCOUNT_DISABLED);
        }

        // 4. 更新登录信息
        String ipAddr = IpUtils.getIpAddr(request);
        adminUser.setLastLoginTime(LocalDateTime.now());
        adminUser.setLastLoginIp(ipAddr);
        adminUserMapper.updateById(adminUser);

        // 5. 生成Token
        String accessToken = jwtUtil.generateAdminAccessToken(
                String.valueOf(adminUser.getId()),
                adminUser.getUsername(),
                String.valueOf(adminUser.getStatus()),
                String.valueOf(adminUser.getRoleType())
        );
        String refreshToken = jwtUtil.generateRefreshToken(String.valueOf(adminUser.getId()));

        // 6. 存储RefreshToken到Redis
        redisCache.set(
                RedisKeyBuild.createKey(RedisKeyManage.ADMIN_LOGIN, adminUser.getId()),
                refreshToken,
                jwtProperties.getRefreshTokenExpire(),
                TimeUnit.MILLISECONDS
        );

        log.info("管理员登录成功：username={}, ip={}", loginDTO.getUsername(), ipAddr);

        return AdminLoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .adminId(adminUser.getId())
                .username(adminUser.getUsername())
                .realName(adminUser.getRealName())
                .build();
    }

    @Override
    public Boolean logout(Long adminId) {
        // 删除Redis中的RefreshToken
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.ADMIN_LOGIN, adminId));
        log.info("管理员登出成功：adminId={}", adminId);
        return true;
    }

    @Override
    public AdminInfoVO getCurrentAdminInfo(Long adminId) {
        AdminUser adminUser = adminUserMapper.selectById(adminId);
        if (adminUser == null) {
            throw new AccountNotFoundException(ErrorConst.ADMIN_NOT_FOUND);
        }

        return AdminInfoVO.builder()
                .id(adminUser.getId())
                .username(adminUser.getUsername())
                .realName(adminUser.getRealName())
                .phone(adminUser.getPhone())
                .email(adminUser.getEmail())
                .avatar(adminUser.getAvatar())
                .roleType(adminUser.getRoleType())
                .status(adminUser.getStatus())
                .lastLoginTime(adminUser.getLastLoginTime())
                .lastLoginIp(adminUser.getLastLoginIp())
                .build();
    }
}

