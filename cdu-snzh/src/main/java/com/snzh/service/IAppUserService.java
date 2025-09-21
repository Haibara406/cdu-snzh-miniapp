package com.snzh.service;

import com.snzh.constants.ValidationConstants;
import com.snzh.domain.ResponseResult;
import com.snzh.domain.dto.UserInfoUpdateDTO;
import com.snzh.domain.dto.UserSearchDTO;
import com.snzh.domain.dto.WxLoginDTO;
import com.snzh.domain.dto.WxPhoneDTO;
import com.snzh.domain.entity.AppUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snzh.domain.vo.PageVo;
import com.snzh.domain.vo.UserInfoVO;
import com.snzh.domain.vo.UserListVO;
import com.snzh.domain.vo.WxLoginVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
public interface IAppUserService extends IService<AppUser> {

    WxLoginVO wxLogin(@Valid WxLoginDTO wxLoginDTO);

    boolean updateUserInfo(Long userId, @Valid UserInfoUpdateDTO userInfoUpdateDTO);

    boolean bindWxPhone(Long userId, @Valid WxPhoneDTO wxPhoneDTO);

    UserInfoVO getUserInfo(Long userId);

    PageVo<UserListVO> getUserOrSearch(@Valid UserSearchDTO userSearchDTO);

    boolean updateStatus(@NotNull(message = ValidationConstants.USER_ID_NOT_NULL) Long id,
                                      @Min(value = 0, message =  ValidationConstants.INCORRECT_STATUS_VALUE)
                                      @Max(value = 1, message =  ValidationConstants.INCORRECT_STATUS_VALUE) Integer status);
}
