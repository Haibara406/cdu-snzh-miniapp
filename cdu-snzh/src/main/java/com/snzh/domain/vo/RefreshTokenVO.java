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
 * @description 刷新Token响应VO
 * @since 2025/9/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "刷新Token响应")
public class RefreshTokenVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "新的访问令牌")
    private String accessToken;
}
