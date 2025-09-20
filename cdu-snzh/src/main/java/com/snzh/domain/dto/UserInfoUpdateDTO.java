package com.snzh.domain.dto;

import com.snzh.constants.RegexConstant;
import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 用户信息更新dto
 * @since 2025/9/20 18:49
 */

@Data
@Schema(description = "用户信息更新DTO")
public class UserInfoUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Pattern(regexp = RegexConstant.REGEX_USERNAME, message = ValidationConstants.INCORRECT_NICKNAME_FORMAT)
    @Length(min = 2, max = 32, message = ValidationConstants.INCORRECT_NICKNAME_FORMAT)
    @Schema(description = "昵称")
    private String nickname;

    @Pattern(regexp = RegexConstant.REGEX_URL, message = ValidationConstants.INCORRECT_AVATAR_URL_FORMAT)
    @Schema(description = "头像")
    private String avatar;

    @Min(value = 0, message = ValidationConstants.INCORRECT_GENDER_VALUE)
    @Max(value = 1, message = ValidationConstants.INCORRECT_GENDER_VALUE)
    @Schema(description = "性别（0女 1男）")
    private Integer gender;

    @Pattern(regexp = RegexConstant.REGEX_CHINESE, message = ValidationConstants.CHINESE_ONLY_FOR_REAL_NAME)
    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "身份证号")
    @Pattern(regexp = RegexConstant.REGEX_ID_CARD, message = ValidationConstants.INCORRECT_ID_NUMBER_FORMAT)
    private String idCard;

    @Schema(description = "手机号")
    @Pattern(regexp = RegexConstant.REGEX_MOBILE, message = ValidationConstants.INCORRECT_PHONE_FORMAT)
    private String phone;

}
