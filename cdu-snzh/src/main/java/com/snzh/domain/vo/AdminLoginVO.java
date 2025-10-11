package com.snzh.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 管理员登录响应VO
 * @since 2025/10/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员登录响应VO")
public class AdminLoginVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "刷新令牌")
    private String refreshToken;

    @Schema(description = "管理员ID")
    private Long adminId;

    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;
}

