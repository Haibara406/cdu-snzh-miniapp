package com.snzh.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 两地距离返回
 * @since 2025/9/27 13:23
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "两地距离返回")
public class CalculateDistanceMapVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "距离（米）")
    private String distance;

    @Schema(description = "格式化后的距离（带单位）")
    private String formattedDistance;

    @Schema(description = "预计行驶时间（秒）")
    private String duration;

    @Schema(description = "格式化后的时间（带单位）")
    private String formattedDuration;
}
