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
 * @description 签到响应VO
 * @since 2025/02/13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "签到响应VO")
public class CheckinResponseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "是否签到成功")
    private Boolean success;

    @Schema(description = "景点名称")
    private String spotName;

    @Schema(description = "景点ID")
    private String spotId;

    @Schema(description = "勋章图片URL")
    private String badgeImageUrl;

    @Schema(description = "距离（米）")
    private String distance;

    @Schema(description = "提示消息")
    private String message;
}
