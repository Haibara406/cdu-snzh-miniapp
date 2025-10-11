package com.snzh.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snzh.constants.ErrorConst;
import com.snzh.domain.dto.*;
import com.snzh.domain.entity.AdminUser;
import com.snzh.domain.vo.AdminUserVO;
import com.snzh.domain.vo.PageVo;
import com.snzh.enums.StatusEnum;
import com.snzh.exceptions.AdminUserException;
import com.snzh.exceptions.DataNotExistException;
import com.snzh.mapper.AdminUserMapper;
import com.snzh.service.IAdminUserService;
import com.snzh.threadlocal.UserContext;
import com.snzh.utils.PageUtil;
import com.snzh.utils.PasswordUtils;
import com.snzh.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author haibara
 * @description 管理员用户管理服务实现
 * @since 2025/10/11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements IAdminUserService {

    private final AdminUserMapper adminUserMapper;

    @Override
    public Boolean createAdmin(AdminUserCreateDTO dto) {
        // 1. 检查用户名是否存在
        Long count = adminUserMapper.selectCount(
                new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, dto.getUsername())
        );
        if (count > 0) {
            throw new AdminUserException(ErrorConst.ADMIN_USERNAME_EXISTS);
        }

        // 2. 创建管理员
        AdminUser adminUser = AdminUser.builder()
                .username(dto.getUsername())
                .password(PasswordUtils.encode(dto.getPassword()))
                .realName(dto.getRealName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .roleType(dto.getRoleType() != null ? dto.getRoleType() : 1)
                .status(StatusEnum.RUN.getCode())
                .build();

        int result = adminUserMapper.insert(adminUser);
        log.info("创建管理员成功：username={}", dto.getUsername());
        return result > 0;
    }

    @Override
    public Boolean updateAdmin(AdminUserUpdateDTO dto) {
        AdminUser adminUser = adminUserMapper.selectById(dto.getId());
        if (StringUtils.isNull(adminUser)) {
            throw new DataNotExistException(ErrorConst.ADMIN_NOT_FOUND);
        }

        // 更新信息
        if (StringUtils.isNotEmpty(dto.getRealName())) {
            adminUser.setRealName(dto.getRealName());
        }
        if (StringUtils.isNotEmpty(dto.getPhone())) {
            adminUser.setPhone(dto.getPhone());
        }
        if (StringUtils.isNotEmpty(dto.getEmail())) {
            adminUser.setEmail(dto.getEmail());
        }
        if (StringUtils.isNotEmpty(dto.getAvatar())) {
            adminUser.setAvatar(dto.getAvatar());
        }

        int result = adminUserMapper.updateById(adminUser);
        log.info("更新管理员信息成功：adminId={}", dto.getId());
        return result > 0;
    }

    @Override
    public Boolean updatePassword(AdminPasswordDTO dto) {
        AdminUser adminUser = adminUserMapper.selectById(dto.getAdminId());
        if (StringUtils.isNull(adminUser)) {
            throw new DataNotExistException(ErrorConst.ADMIN_NOT_FOUND);
        }

        // 加密新密码
        adminUser.setPassword(PasswordUtils.encode(dto.getNewPassword()));
        int result = adminUserMapper.updateById(adminUser);
        
        log.info("修改管理员密码成功：adminId={}", dto.getAdminId());
        return result > 0;
    }

    @Override
    public Boolean batchDelete(java.util.List<Long> adminIds) {
        if (StringUtils.isEmpty(adminIds)) {
            return false;
        }

        // 获取当前操作的管理员ID，防止删除自己
        String currentUserIdStr = UserContext.get("userId");
        if (StringUtils.isNotEmpty(currentUserIdStr)) {
            Long currentAdminId = Long.valueOf(currentUserIdStr);
            if (adminIds.contains(currentAdminId)) {
                throw new AdminUserException("不能删除自己的账号");
            }
        }

        // 检查是否包含超级管理员
        java.util.List<AdminUser> adminList = adminUserMapper.selectBatchIds(adminIds);
        boolean hasSuperAdmin = adminList.stream().anyMatch(admin -> admin.getRoleType() == 0);
        if (hasSuperAdmin) {
            throw new AdminUserException(ErrorConst.CANNOT_DELETE_SUPER_ADMIN);
        }

        // 批量修改状态为禁用（逻辑删除）- 优化为批量更新
        int result = adminUserMapper.update(null,
                new LambdaUpdateWrapper<AdminUser>()
                        .in(AdminUser::getId, adminIds)
                        .set(AdminUser::getStatus, StatusEnum.STOP.getCode())
        );

        log.info("批量删除管理员成功：adminIds={}, 影响行数={}", adminIds, result);
        return result > 0;
    }

    @Override
    public Boolean updateStatus(Long adminId, Integer status) {
        // 防止禁用自己的账号
        String currentUserIdStr = UserContext.get("userId");
        if (StringUtils.isNotEmpty(currentUserIdStr)) {
            Long currentAdminId = Long.valueOf(currentUserIdStr);
            if (currentAdminId.equals(adminId) && StatusEnum.STOP.getCode().equals(status)) {
                throw new AdminUserException("不能禁用自己的账号");
            }
        }

        AdminUser adminUser = adminUserMapper.selectById(adminId);
        if (StringUtils.isNull(adminUser)) {
            throw new DataNotExistException(ErrorConst.ADMIN_NOT_FOUND);
        }

        // 不能禁用超级管理员
        if (adminUser.getRoleType() == 0 && StatusEnum.STOP.getCode().equals(status)) {
            throw new AdminUserException(ErrorConst.CANNOT_DISABLE_SUPER_ADMIN);
        }

        adminUser.setStatus(status);
        int result = adminUserMapper.updateById(adminUser);
        
        log.info("修改管理员状态成功：adminId={}, status={}", adminId, status);
        return result > 0;
    }

    @Override
    public PageVo<AdminUserVO> getAdminList(AdminUserQueryDTO dto) {
        IPage<AdminUser> page = PageUtil.getPageParams(dto);
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        
        // 条件查询
        if (StringUtils.isNotNull(dto)) {
            wrapper.like(StringUtils.isNotEmpty(dto.getUsername()), AdminUser::getUsername, dto.getUsername())
                    .like(StringUtils.isNotEmpty(dto.getRealName()), AdminUser::getRealName, dto.getRealName())
                    .eq(StringUtils.isNotNull(dto.getRoleType()), AdminUser::getRoleType, dto.getRoleType())
                    .eq(StringUtils.isNotNull(dto.getStatus()), AdminUser::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(AdminUser::getCreateTime);

        IPage<AdminUser> adminPage = adminUserMapper.selectPage(page, wrapper);
        return PageUtil.convertPage(adminPage, admin -> BeanUtil.copyProperties(admin, AdminUserVO.class));
    }

    @Override
    public AdminUserVO getAdminById(Long adminId) {
        AdminUser adminUser = adminUserMapper.selectById(adminId);
        if (StringUtils.isNull(adminUser)) {
            throw new DataNotExistException(ErrorConst.ADMIN_NOT_FOUND);
        }
        return BeanUtil.copyProperties(adminUser, AdminUserVO.class);
    }
}

