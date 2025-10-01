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
 * @description 景点地图返回vo
 * @since 2025/10/1 18:37
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "景点地图返回vo")
public class NearByScenicSpotVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "景点名称")
    private String name;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "距离（米）")
    private String distance;

    @Schema(description = "格式化后的距离（带单位）")
    private String formattedDistance;

    @Schema(description = "经度")
    private String longitude;

    @Schema(description = "纬度")
    private String latitude;

    @Schema(description = "高德地图POI ID")
    private String poiId;

    @Schema(description = "联系电话")
    private String contactPhone;
}
