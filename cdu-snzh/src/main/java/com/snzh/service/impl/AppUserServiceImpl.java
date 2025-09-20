package com.snzh.service.impl;

import com.snzh.domain.entity.AppUser;
import com.snzh.mapper.AppUserMapper;
import com.snzh.service.IAppUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
@Service
public class AppUserServiceImpl extends ServiceImpl<AppUserMapper, AppUser> implements IAppUserService {

}
