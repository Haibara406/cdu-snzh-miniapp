package com.snzh.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.snzh.constants.ErrorConst;
import com.snzh.domain.dto.UserInfoUpdateDTO;
import com.snzh.domain.dto.UserSearchDTO;
import com.snzh.domain.dto.WxLoginDTO;
import com.snzh.domain.dto.WxPhoneDTO;
import com.snzh.domain.entity.AppUser;
import com.snzh.domain.properties.JwtProperties;
import com.snzh.domain.vo.PageVo;
import com.snzh.domain.vo.RefreshTokenVO;
import com.snzh.domain.vo.UserInfoVO;
import com.snzh.domain.vo.UserListVO;
import com.snzh.domain.vo.WxLoginVO;
import com.snzh.enums.RedisKeyManage;
import com.snzh.enums.StatusEnum;
import com.snzh.enums.UploadEnum;
import com.snzh.exceptions.*;
import com.snzh.mapper.AppUserMapper;
import com.snzh.redis.RedisCache;
import com.snzh.redis.RedisKeyBuild;
import com.snzh.service.IAppUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snzh.threadlocal.UserContext;
import com.snzh.utils.FileUploadUtils;
import com.snzh.utils.JwtUtil;
import com.snzh.utils.PageUtil;
import com.snzh.utils.StringUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final FileUploadUtils fileUploadUtils;


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
            String refreshToken = jwtUtil.generateRefreshToken(Long.toString(appUser.getId()), Integer.toString(appUser.getStatus()));
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
    @Transactional(rollbackFor = Exception.class)
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
        appUser.setId(userId);

        return updateById(appUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
            if(StringUtils.isNotNull(userSearchDTO.getCreateTimeStart())
                    && StringUtils.isNotNull(userSearchDTO.getCreateTimeEnd())
                    && userSearchDTO.getCreateTimeStart().before(userSearchDTO.getCreateTimeEnd())){
                wrapper.between(AppUser::getCreateTime, userSearchDTO.getCreateTimeStart(), userSearchDTO.getCreateTimeEnd());
            }
        }

        IPage<AppUser> userPage = userMapper.selectPage(page, wrapper);
        return PageUtil.convertPage(userPage, user -> BeanUtil.copyProperties(user, UserListVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        if(!userMapper.exists(Wrappers.lambdaQuery(AppUser.class).eq(AppUser::getId, id))){
            throw new AccountNotFoundException(ErrorConst.ACCOUNT_NOT_FOUND);
        }

        AppUser user = AppUser.builder()
                .status(status)
                .id(id)
                .build();

        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean uploadAvatar(MultipartFile file, String name) {
        Long userId = UserContext.get("userId");
        if(userId == null){
            throw new DataNotExistException(ErrorConst.DATA_NOT_FOUND);
        }
        AppUser appUser = userMapper.selectOne(Wrappers.lambdaQuery(AppUser.class).eq(AppUser::getId, userId));
        if(appUser == null){
            throw new AccountNotFoundException(ErrorConst.ACCOUNT_NOT_FOUND);
        }
        try{
            String avatarUrl = fileUploadUtils.upload(UploadEnum.USER_AVATAR, file, name);
            appUser.setAvatar(avatarUrl);
            userMapper.updateById(appUser);
            return true;
        }catch (FileUploadException e){
            log.error("{}上传失败", UploadEnum.USER_AVATAR.getDescription(), e);
            return false;
        }catch (Exception e){
            log.error("{}上传出现未知异常", UploadEnum.USER_AVATAR.getDescription(), e);
            return false;
        }
    }

    @Override
    public RefreshTokenVO refreshToken(String refreshToken) {
        try {
            // 1. 验证 Refresh Token 的有效性
            Claims claims = jwtUtil.parseToken(refreshToken);
            String userId = claims.getSubject();
            String userType = claims.get("userType", String.class);
            String status = claims.get("status", String.class);

            // 2. 验证用户类型
            if (!"USER".equals(userType)) {
                throw new LoginFailedException(ErrorConst.INVALID_TOKEN);
            }

            // 3. 验证 Redis 中存储的 Refresh Token 是否匹配
            String storedRefreshToken = redisCache.get(
                    RedisKeyBuild.createKey(RedisKeyManage.USER_LOGIN, userId),
                    String.class
            );
            
            if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
                throw new LoginFailedException(ErrorConst.TOKEN_EXPIRED);
            }

            // 4. 检查用户状态
            AppUser appUser = userMapper.selectById(userId);
            if (appUser == null) {
                throw new AccountNotFoundException(ErrorConst.ACCOUNT_NOT_FOUND);
            }
            if (StatusEnum.STOP.getCode().equals(appUser.getStatus())) {
                throw new AccountLockedException(ErrorConst.ACCOUNT_LOCKED);
            }

            // 5. 生成新的 Access Token
            String newAccessToken = jwtUtil.generateAccessToken(userId, status);

            log.info("用户刷新Token成功：userId={}", userId);

            return RefreshTokenVO.builder()
                    .accessToken(newAccessToken)
                    .build();

        } catch (RuntimeException e) {
            log.error("用户刷新Token失败", e);
            throw new LoginFailedException(ErrorConst.INVALID_TOKEN);
        }
    }
}
