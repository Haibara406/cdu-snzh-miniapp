package com.snzh.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author haibara
 * @description 勋章收藏VO
 * @since 2025/02/13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "勋章收藏VO")
public class BadgeCollectionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "景点ID")
    private String spotId;

    @Schema(description = "景点名称")
    private String spotName;

    @Schema(description = "勋章图片URL")
    private String badgeImageUrl;

    @Schema(description = "首次签到时间")
    private LocalDateTime firstCheckinTime;

    @Schema(description = "签到次数")
    private Integer checkinCount;
}
