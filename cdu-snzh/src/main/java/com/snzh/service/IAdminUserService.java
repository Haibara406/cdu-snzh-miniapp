package com.snzh.service;

import com.snzh.domain.dto.*;
import com.snzh.domain.vo.AdminUserVO;
import com.snzh.domain.vo.PageVo;

import java.util.List;

/**
 * @author haibara
 * @description 管理员用户管理服务接口
 * @since 2025/10/11
 */
public interface IAdminUserService {

    /**
     * 创建管理员
     */
    Boolean createAdmin(AdminUserCreateDTO dto);

    /**
     * 更新管理员信息
     */
    Boolean updateAdmin(AdminUserUpdateDTO dto);

    /**
     * 修改管理员密码
     */
    Boolean updatePassword(AdminPasswordDTO dto);

    /**
     * 批量删除管理员（修改状态为禁用）
     */
    Boolean batchDelete(List<Long> adminIds);

    /**
     * 修改管理员状态
     */
    Boolean updateStatus(Long adminId, Integer status);

    /**
     * 管理员列表查询
     */
    PageVo<AdminUserVO> getAdminList(AdminUserQueryDTO dto);

    /**
     * 根据ID获取管理员详情
     */
    AdminUserVO getAdminById(Long adminId);
}

