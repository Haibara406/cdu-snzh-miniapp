package com.snzh.domain.dto;

import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 微信登录 dto
 * @since 2025/9/20 17:00
 */
@Data
@Schema(description = "微信登录请求DTO")
public class WxLoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = ValidationConstants.CODE_NOT_NULL)
    @Schema(description = "微信临时登录凭证")
    private String code;
}
