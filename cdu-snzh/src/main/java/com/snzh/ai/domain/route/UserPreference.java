package com.snzh.ai.domain.route;

import com.snzh.ai.enums.TravelStrategy;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

/**
 * @author haibara
 * @description 用户偏好信息
 * @since 2025/11/6
 */
@Data
@Schema(description = "用户游玩偏好信息")
public class UserPreference {
    
    @Schema(description = "游玩时长（小时）", example = "6")
    private int duration;
    
    @Schema(description = "是否有小孩")
    private boolean hasChildren;
    
    @Schema(description = "是否有老人")
    private boolean hasElderly;
    
    @Deprecated
    @Schema(description = "是否徒步（已废弃，请使用strategies）", deprecated = true)
    private boolean hiking;
    
    @Deprecated
    @Schema(description = "是否摄影（已废弃，请使用strategies）", deprecated = true)
    private boolean photography;
    
    @Deprecated
    @Schema(description = "是否休闲游（已废弃，请使用strategies）", deprecated = true)
    private boolean leisure;
    
    @Deprecated
    @Schema(description = "是否喜欢竹文化（已废弃，请使用strategies）", deprecated = true)
    private boolean bambooCulture;
    
    @Schema(description = "游玩日期", example = "2025-11-10")
    private LocalDate visitDate;
    
    @Schema(description = "天气状况", example = "晴")
    private String weatherCondition;
    
    @Schema(description = "温度（摄氏度）", example = "22")
    private int temperature;
    
    @Schema(description = "天气描述", example = "晴天，适合出游")
    private String weatherDesc;
    
    @Schema(description = "是否自驾游")
    private boolean selfDriving;
    
    @Schema(description = "是否使用电动车")
    private boolean hasElectricVehicle;
    
    @Schema(description = "游玩策略集合，支持多策略组合", example = "[\"FAMILY_TRIP\", \"PHOTOGRAPHY\"]")
    private Set<TravelStrategy> strategies;
}

