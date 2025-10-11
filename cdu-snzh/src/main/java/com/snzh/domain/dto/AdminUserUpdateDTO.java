package com.snzh.domain.dto;

import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 更新管理员信息DTO
 * @since 2025/10/11
 */
@Data
@Schema(description = "更新管理员信息DTO")
public class AdminUserUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = ValidationConstants.ADMIN_ID_NOT_NULL)
    @Schema(description = "管理员ID")
    private Long id;

    @Size(min = 2, max = 50, message = ValidationConstants.ADMIN_REAL_NAME_LENGTH)
    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像URL")
    private String avatar;
}

