package com.snzh.domain.dto;

import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 管理员登录请求DTO
 * @since 2025/10/11
 */
@Data
@Schema(description = "管理员登录请求DTO")
public class AdminLoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = ValidationConstants.ADMIN_USERNAME_NOT_NULL)
    @Schema(description = "登录账号")
    private String username;

    @NotBlank(message = ValidationConstants.ADMIN_PASSWORD_NOT_NULL)
    @Schema(description = "登录密码")
    private String password;
}

