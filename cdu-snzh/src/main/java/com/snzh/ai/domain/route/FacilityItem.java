package com.snzh.ai.domain.route;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author haibara
 * @description 设施项
 * @since 2025/11/6
 */
@Data
@Schema(description = "设施项信息")
public class FacilityItem {
    
    @Schema(description = "设施名称", example = "竹海人家餐厅")
    private String name;
    
    @Schema(description = "详细地址", example = "蜀南竹海景区内")
    private String address;
    
    @Schema(description = "营业时间", example = "08:00-20:00")
    private String openTime;
    
    @Schema(description = "联系电话", example = "0831-1234567")
    private String contactPhone;
    
    @Schema(description = "距离描述", example = "距景点500米")
    private String distance;
    
    @Schema(description = "推荐理由", example = "离景点最近，环境优美")
    private String reason;
}

