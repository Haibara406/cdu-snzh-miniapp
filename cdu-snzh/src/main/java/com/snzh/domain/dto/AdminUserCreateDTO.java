package com.snzh.domain.dto;

import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 创建管理员DTO
 * @since 2025/10/11
 */
@Data
@Schema(description = "创建管理员DTO")
public class AdminUserCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = ValidationConstants.ADMIN_USERNAME_NOT_NULL)
    @Size(min = 3, max = 50, message = ValidationConstants.ADMIN_USERNAME_LENGTH)
    @Schema(description = "登录账号")
    private String username;

    @NotBlank(message = ValidationConstants.ADMIN_PASSWORD_NOT_NULL)
    @Size(min = 6, max = 20, message = ValidationConstants.ADMIN_PASSWORD_LENGTH)
    @Schema(description = "登录密码")
    private String password;

    @Size(min = 2, max = 50, message = ValidationConstants.ADMIN_REAL_NAME_LENGTH)
    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "角色类型：0-超级管理员，1-普通管理员", example = "1")
    private Integer roleType;
}

