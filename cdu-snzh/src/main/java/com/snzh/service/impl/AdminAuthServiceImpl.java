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
import com.snzh.exceptions.TooManyLoginAttemptsException;
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

    /** 最大登录失败次数 */
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    
    /** 账号锁定时间（分钟） */
    private static final int LOCK_TIME_MINUTES = 30;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminLoginVO login(AdminLoginDTO loginDTO, HttpServletRequest request) {
        String ipAddr = IpUtils.getIpAddr(request);
        RedisKeyBuild loginFailKey = RedisKeyBuild.createKey(RedisKeyManage.ADMIN_LOGIN_FAIL, loginDTO.getUsername());

        // 0. 检查登录失败次数
        Integer failCount = redisCache.get(loginFailKey, Integer.class);
        if (failCount != null && failCount >= MAX_LOGIN_ATTEMPTS) {
            Long ttl = redisCache.getExpire(loginFailKey);
            long remainMinutes = ttl != null ? (ttl / 60) : LOCK_TIME_MINUTES;
            log.warn("管理员登录失败次数过多：username={}, ip={}, failCount={}", loginDTO.getUsername(), ipAddr, failCount);
            throw new TooManyLoginAttemptsException(
                String.format(ErrorConst.ACCOUNT_TEMP_LOCKED, remainMinutes)
            );
        }

        // 1. 查询管理员账号
        AdminUser adminUser = adminUserMapper.selectOne(
                Wrappers.lambdaQuery(AdminUser.class)
                        .eq(AdminUser::getUsername, loginDTO.getUsername())
        );

        if (adminUser == null) {
            // 记录登录失败
            recordLoginFailure(loginFailKey);
            log.warn("管理员登录失败-账号不存在：username={}, ip={}", loginDTO.getUsername(), ipAddr);
            throw new AccountNotFoundException(ErrorConst.ADMIN_LOGIN_ERROR);
        }

        // 2. 验证密码
        if (!PasswordUtils.matches(loginDTO.getPassword(), adminUser.getPassword())) {
            // 记录登录失败
            recordLoginFailure(loginFailKey);
            log.warn("管理员登录失败-密码错误：username={}, ip={}", loginDTO.getUsername(), ipAddr);
            throw new AdminLoginException(ErrorConst.ADMIN_LOGIN_ERROR);
        }

        // 3. 检查账号状态
        if (StatusEnum.STOP.getCode().equals(adminUser.getStatus())) {
            throw new AccountLockedException(ErrorConst.ADMIN_ACCOUNT_DISABLED);
        }

        // 4. 登录成功，清除失败记录
        redisCache.del(loginFailKey);

        // 5. 更新登录信息
        adminUser.setLastLoginTime(LocalDateTime.now());
        adminUser.setLastLoginIp(ipAddr);
        adminUserMapper.updateById(adminUser);

        // 6. 生成Token
        String accessToken = jwtUtil.generateAdminAccessToken(
                String.valueOf(adminUser.getId()),
                adminUser.getUsername(),
                String.valueOf(adminUser.getStatus()),
                String.valueOf(adminUser.getRoleType())
        );
        String refreshToken = jwtUtil.generateRefreshToken(String.valueOf(adminUser.getId()));

        // 7. 存储RefreshToken到Redis
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

    /**
     * 记录登录失败次数
     */
    private void recordLoginFailure(RedisKeyBuild loginFailKey) {
        Integer failCount = redisCache.get(loginFailKey, Integer.class);
        if (failCount == null) {
            failCount = 0;
        }
        failCount++;
        
        // 设置过期时间（锁定时间）
        redisCache.set(loginFailKey, failCount, LOCK_TIME_MINUTES, TimeUnit.MINUTES);
        
        log.debug("记录登录失败：key={}, failCount={}", loginFailKey.getRealKey(), failCount);
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

