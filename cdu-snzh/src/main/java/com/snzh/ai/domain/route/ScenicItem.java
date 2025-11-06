package com.snzh.ai.domain.route;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author haibara
 * @description 路线中的景点项
 * @since 2025/11/6
 */
@Data
@Schema(description = "景点项信息")
public class ScenicItem {
    
    @Schema(description = "景点名称", example = "七彩飞瀑")
    private String name;
    
    @Schema(description = "建议游览时长（分钟）", example = "60")
    private int duration;
    
    @Schema(description = "推荐理由", example = "晴天彩虹效果最佳")
    private String reason;
    
    @Schema(description = "游玩提示", example = "建议携带防晒用品")
    private String tips;
}

