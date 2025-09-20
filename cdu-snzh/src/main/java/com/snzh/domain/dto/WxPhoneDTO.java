package com.snzh.domain.dto;

import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 获取手机号 dto
 * @since 2025/9/20 21:32
 */
@Data
@Schema(description = "微信手机号获取DTO")
public class WxPhoneDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

//    @NotBlank(message = ValidationConstants.ENCRYPTED_DATA_NOT_NULL)
//    @Schema(description = "微信加密数据")
//    private String encryptedData;
//
//    @NotBlank(message = ValidationConstants.IV_NOT_NULL)
//    @Schema(description = "加密算法的初始向量")
//    private String iv;

    @NotBlank(message = ValidationConstants.CODE_NOT_NULL)
    @Schema(description = "微信临时登录凭证")
    private String code;
}