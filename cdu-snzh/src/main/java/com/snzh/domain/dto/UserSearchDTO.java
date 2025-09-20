package com.snzh.domain.dto;

import com.snzh.constants.RegexConstant;
import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author haibara
 * @description 客户端搜索用户 dto
 * @since 2025/9/20 21:50
 */

@Data
@Schema(description = "搜索用户列表请求")
public class UserSearchDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名")
    @Pattern(regexp = RegexConstant.REGEX_USERNAME, message = ValidationConstants.INCORRECT_NICKNAME_FORMAT)
    @Length(min = 2, max = 32, message = ValidationConstants.INCORRECT_NICKNAME_FORMAT)
    private String nickname;

    @Schema(description = "是否禁用：0-否，1-是", allowableValues = {"0", "1"})
    @Min(value = 0, message =  ValidationConstants.INCORRECT_STATUS_VALUE)
    @Max(value = 1, message =  ValidationConstants.INCORRECT_STATUS_VALUE)
    private Integer status;

    @Pattern(regexp = RegexConstant.REGEX_CHINESE, message = ValidationConstants.CHINESE_ONLY_FOR_REAL_NAME)
    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "创建时间开始")
    private Date createTimeStart;

    @Schema(description = "创建时间结束")
    private Date createTimeEnd;
}
