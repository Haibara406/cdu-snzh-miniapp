package com.snzh.ai.domain.route;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author haibara
 * @description 景点信息封装类
 * @since 2025/11/6
 */
@Data
@Schema(description = "景点信息")
public class ScenicInfo {
    
    @Schema(description = "景点ID", example = "1")
    private Long id;
    
    @Schema(description = "景点名称", example = "七彩飞瀑")
    private String name;
    
    @Schema(description = "景点描述")
    private String description;
    
    @Schema(description = "经度", example = "104.998465")
    private String longitude;
    
    @Schema(description = "纬度", example = "28.470787")
    private String latitude;
    
    @Schema(description = "难度等级：1-简单，2-中等，3-困难", example = "1")
    private int difficulty;
    
    @Schema(description = "建议游览时间（分钟）", example = "60")
    private int recommendTime;
    
    @Schema(description = "是否适合儿童")
    private boolean suitableForChildren;
    
    @Schema(description = "是否适合老人")
    private boolean suitableForElderly;
    
    @Schema(description = "是否适合摄影")
    private boolean suitableForPhotography;
    
    @Schema(description = "是否适合雨天")
    private boolean rainyDayFriendly;
    
    @Schema(description = "景点标签")
    private String[] tags;
    
    @Schema(description = "优先级（基于综合因素计算）", example = "100")
    private int priority;
    
    @Schema(description = "景点评分（0-5分）", example = "4.5")
    private double scenicRating;
    
    @Schema(description = "拥堵影响：1=空闲，2=适中，3=繁忙", example = "1")
    private int congestionImpact;
    
    @Schema(description = "天气影响：1=低（晴雨皆宜），2=中，3=高（天气敏感）", example = "2")
    private int weatherImpact;
    
    @Schema(description = "距离描述", example = "距入口3公里")
    private String distance;
}

