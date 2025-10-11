package com.snzh.domain.dto;

import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 修改管理员密码DTO
 * @since 2025/10/11
 */
@Data
@Schema(description = "修改管理员密码DTO")
public class AdminPasswordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = ValidationConstants.ADMIN_ID_NOT_NULL)
    @Schema(description = "管理员ID")
    private Long adminId;

    @NotBlank(message = ValidationConstants.ADMIN_PASSWORD_NOT_NULL)
    @Size(min = 6, max = 20, message = ValidationConstants.ADMIN_PASSWORD_LENGTH)
    @Schema(description = "新密码")
    private String newPassword;
}

