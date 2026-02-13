package com.snzh.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.snzh.domain.base.BaseTableData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author haibara
 * @description 景区签到记录实体类
 * @since 2025/02/13
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("scenic_checkin")
@Schema(description = "景区签到记录实体类")
public class ScenicCheckin extends BaseTableData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "签到记录ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "景点ID")
    private String spotId;

    @Schema(description = "景点名称")
    private String spotName;

    @Schema(description = "签到时间")
    private LocalDateTime checkinTime;

    @Schema(description = "经度")
    private String longitude;

    @Schema(description = "纬度")
    private String latitude;

    @Schema(description = "距离景点中心的距离（米）")
    private String distance;

    @Schema(description = "勋章图片URL")
    private String badgeImageUrl;

    @Schema(description = "围栏半径（米）")
    private String fenceRadius;
}
