package com.snzh.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * @author haibara
 * @description 签到历史查询DTO
 * @since 2025/02/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "签到历史查询DTO")
public class CheckinHistoryQueryDTO extends BasePageDto {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID（管理端使用）")
    private Long userId;

    @Schema(description = "景点ID")
    private String spotId;
}
