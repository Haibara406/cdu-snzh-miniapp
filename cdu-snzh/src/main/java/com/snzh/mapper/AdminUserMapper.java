package com.snzh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.snzh.domain.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author haibara
 * @description 管理员用户Mapper
 * @since 2025/10/11
 */
@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
}

