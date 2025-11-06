package com.snzh.ai.domain.route;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author haibara
 * @description 路线推荐结果
 * @since 2025/11/6
 */
@Data
@Schema(description = "路线推荐结果")
public class RouteRecommendation {
    
    @Schema(description = "推荐标题", example = "蜀南竹海6小时精品游")
    private String title;
    
    @Schema(description = "天气信息", example = "今日晴天，温度22°C，适合游览")
    private String weatherInfo;
    
    @Schema(description = "路线段列表")
    private List<RouteSegment> segments;
    
    @Schema(description = "游玩提示")
    private String tips;
    
    @Schema(description = "行程总结")
    private String summary;
}

