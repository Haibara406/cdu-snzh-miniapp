package com.snzh.domain.dto;

import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 管理端更新用户状态 dto
 * @since 2025/9/20 21:59
 */
@Data
@Schema
public class UpdateRoleStatusDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = ValidationConstants.USER_ID_NOT_NULL)
    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "是否禁用：0-否，1-是", allowableValues = {"0", "1"})
    @Min(value = 0, message =  ValidationConstants.INCORRECT_STATUS_VALUE)
    @Max(value = 1, message =  ValidationConstants.INCORRECT_STATUS_VALUE)
    private Integer status;
}
