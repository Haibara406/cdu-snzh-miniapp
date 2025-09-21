package com.snzh.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.snzh.constants.ErrorConst;
import com.snzh.domain.ResponseResult;
import com.snzh.domain.dto.UserInfoUpdateDTO;
import com.snzh.domain.dto.UserSearchDTO;
import com.snzh.domain.dto.WxLoginDTO;
import com.snzh.domain.dto.WxPhoneDTO;
import com.snzh.domain.entity.AppUser;
import com.snzh.domain.properties.JwtProperties;
import com.snzh.domain.vo.PageVo;
import com.snzh.domain.vo.UserInfoVO;
import com.snzh.domain.vo.UserListVO;
import com.snzh.domain.vo.WxLoginVO;
import com.snzh.enums.RedisKeyManage;
import com.snzh.enums.StatusEnum;
import com.snzh.exceptions.*;
import com.snzh.mapper.AppUserMapper;
import com.snzh.redis.RedisCache;
import com.snzh.redis.RedisKeyBuild;
import com.snzh.service.IAppUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snzh.utils.JwtUtil;
import com.snzh.utils.PageUtil;
import com.snzh.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AppUserServiceImpl extends ServiceImpl<AppUserMapper, AppUser> implements IAppUserService {

    private final AppUserMapper userMapper;
    private final RedisCache redisCache;
    private final JwtUtil jwtUtil;
    private final WxMaService wxMaService;
    private final JwtProperties jwtProperties;


    @Override
    public WxLoginVO wxLogin(WxLoginDTO wxLoginDTO) {
        try {
            // 获取微信用户openid和session_key
            WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(wxLoginDTO.getCode());
            String openid = sessionInfo.getOpenid();

            // 查询用户是否存在
            AppUser appUser = userMapper.selectOne(Wrappers.lambdaQuery(AppUser.class).eq(AppUser::getOpenid, openid));

            // 用户不存在则注册
            if(appUser == null){
                appUser = AppUser.builder()
                        .openid(openid)
                        .status(StatusEnum.RUN.getCode())
                        .build();
                userMapper.insert(appUser);
            }

            // 判断账户是否被封禁
            if(appUser.getStatus().equals(StatusEnum.STOP.getCode())){
                throw new AccountLockedException(ErrorConst.ACCOUNT_LOCKED);
            }

            // 若一切正常则生成token
            String accessToken = jwtUtil.generateAccessToken(Long.toString(appUser.getId()), Integer.toString(appUser.getStatus()));
            String refreshToken = jwtUtil.generateRefreshToken(Long.toString(appUser.getId()));
            redisCache.set(RedisKeyBuild.createKey(RedisKeyManage.USER_LOGIN, appUser.getId()), refreshToken, jwtProperties.getRefreshTokenExpire(), TimeUnit.MILLISECONDS);

            return WxLoginVO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .openid(openid)
                    .userId(appUser.getId())
                    .build();

        } catch (WxErrorException e) {
            log.error("微信登录异常", e);
            throw new LoginFailedException(ErrorConst.LOGIN_FAILED);
        }
    }

    @Override
    public boolean updateUserInfo(Long userId, UserInfoUpdateDTO userInfoUpdateDTO) {
        if(userId == null || userInfoUpdateDTO == null){
            throw new DataNotExistException(ErrorConst.DATA_NOT_FOUND);
        }

        // 获取当前用户
        AppUser appUser = userMapper.selectOne(Wrappers.lambdaQuery(AppUser.class).eq(AppUser::getId, userId));
        if(appUser == null){
            throw new AccountNotFoundException(ErrorConst.ACCOUNT_NOT_FOUND);
        }
        BeanUtil.copyProperties(userInfoUpdateDTO, appUser, true);

        return updateById(appUser);
    }

    @Override
    public boolean bindWxPhone(Long userId, WxPhoneDTO wxPhoneDTO) {
        if (userId == null || wxPhoneDTO == null) {
            throw new DataNotExistException(ErrorConst.DATA_NOT_FOUND);
        }

        try {
            // 获取手机号
            WxMaPhoneNumberInfo phoneNumberInfo = wxMaService.getUserService().getPhoneNumber(wxPhoneDTO.getCode());
            String phoneNumber = phoneNumberInfo.getPhoneNumber();
            // 查询当前用户
            AppUser appUser = userMapper.selectOne(Wrappers.lambdaQuery(AppUser.class).eq(AppUser::getId, userId));
            if(appUser == null){
                throw new AccountNotFoundException(ErrorConst.ACCOUNT_NOT_FOUND);
            }
            appUser.setPhone(phoneNumber);
            return updateById(appUser);

        } catch (WxErrorException e) {
            log.error("获取微信手机号异常", e);
            throw new GetPhoneNumberException(ErrorConst.GET_PHONE_NUMBER_ERROR);
        }
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        if(userId == null){
            throw new AccountNotFoundException(ErrorConst.ACCOUNT_NOT_FOUND);
        }

        AppUser appUser = userMapper.selectOne(Wrappers.lambdaQuery(AppUser.class).eq(AppUser::getId, userId));
        if(appUser == null){
            throw new AccountNotFoundException(ErrorConst.ACCOUNT_NOT_FOUND);
        }

        // 构建返回对象
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtil.copyProperties(appUser, userInfoVO);

        return userInfoVO;
    }

    @Override
    public PageVo<UserListVO> getUserOrSearch(UserSearchDTO userSearchDTO) {

        IPage<AppUser> page = PageUtil.getPageParams(userSearchDTO);
        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<>();
        if(userSearchDTO != null){
            wrapper.like(StringUtils.isNotEmpty(userSearchDTO.getNickname()), AppUser::getNickname, userSearchDTO.getNickname())
                    .like(StringUtils.isNotEmpty(userSearchDTO.getRealName()), AppUser::getRealName, userSearchDTO.getRealName())
                    .eq(StringUtils.isNotNull(userSearchDTO.getStatus()), AppUser::getStatus, userSearchDTO.getStatus());
            if(StringUtils.isNotNull(userSearchDTO.getCreateTimeStart()) && StringUtils.isNotNull(userSearchDTO.getCreateTimeEnd())){
                wrapper.between(AppUser::getCreateTime, userSearchDTO.getCreateTimeStart(), userSearchDTO.getCreateTimeEnd());
            }
        }

        IPage<AppUser> userPage = userMapper.selectPage(page, wrapper);
        return PageUtil.convertPage(userPage, user -> BeanUtil.copyProperties(user, UserListVO.class));
    }

    @Override
    public ResponseResult<Void> updateStatus(Long id, Integer status) {
        AppUser user = AppUser.builder()
                .status(status)
                .id(id)
                .build();

        return updateById(user) ? ResponseResult.success() : ResponseResult.failure();
    }
}
