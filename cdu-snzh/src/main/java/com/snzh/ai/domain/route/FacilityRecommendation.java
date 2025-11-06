package com.snzh.ai.domain.route;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author haibara
 * @description 基础设施推荐
 * @since 2025/11/6
 */
@Data
@Schema(description = "基础设施推荐信息")
public class FacilityRecommendation {
    
    @Schema(description = "推荐餐厅列表")
    private List<FacilityItem> restaurants;
    
    @Schema(description = "推荐住宿列表")
    private List<FacilityItem> accommodations;
    
    @Schema(description = "附近卫生间列表")
    private List<FacilityItem> toilets;
    
    @Schema(description = "停车场列表")
    private List<FacilityItem> parkings;
    
    @Schema(description = "充电桩列表")
    private List<FacilityItem> chargingStations;
    
    @Schema(description = "其他服务设施列表")
    private List<FacilityItem> services;
    
    @Schema(description = "设施相关提示")
    private String tips;
}

