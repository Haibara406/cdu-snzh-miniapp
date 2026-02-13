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
 * @description 签到历史VO
 * @since 2025/02/13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "签到历史VO")
public class CheckinHistoryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "签到记录ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "景点名称")
    private String spotName;

    @Schema(description = "景点ID")
    private String spotId;

    @Schema(description = "签到时间")
    private LocalDateTime checkinTime;

    @Schema(description = "勋章图片URL")
    private String badgeImageUrl;

    @Schema(description = "距离（米）")
    private String distance;
}
