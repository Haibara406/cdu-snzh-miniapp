package com.snzh.ai.domain.route;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author haibara
 * @description 路线段
 * @since 2025/11/6
 */
@Data
@Schema(description = "路线段信息")
public class RouteSegment {
    
    @Schema(description = "时间范围", example = "09:00-12:00")
    private String timeRange;
    
    @Schema(description = "时段描述", example = "上午")
    private String period;
    
    @Schema(description = "景点列表")
    private List<ScenicItem> scenics;
    
    @Schema(description = "路线描述")
    private String description;
    
    @Schema(description = "基础设施推荐")
    private FacilityRecommendation facilityRecommendation;
}

