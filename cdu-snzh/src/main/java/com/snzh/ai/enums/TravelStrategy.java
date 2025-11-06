package com.snzh.ai.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author haibara
 * @description 游玩策略枚举
 * @since 2025/11/6
 */
@Schema(description = "游玩策略枚举")
public enum TravelStrategy {
    
    @Schema(description = "徒步挑战 - 适合体力好、喜欢运动的游客，路线距离较长，气候舒适")
    HIKING_CHALLENGE("hiking_challenge", "徒步挑战", "适合体力好、喜欢运动的游客，路线距离较长，气候舒适"),
    
    @Schema(description = "摄影爱好 - 适合摄影爱好者，推荐景点评分高、视野开阔的地方")
    PHOTOGRAPHY("photography", "摄影爱好", "适合摄影爱好者，推荐景点评分高、视野开阔的地方"),
    
    @Schema(description = "休闲散步 - 适合轻松游览，距离较短，路况平缓清净")
    LEISURE_WALK("leisure_walk", "休闲散步", "适合轻松游览，距离较短，路况平缓清净"),
    
    @Schema(description = "自然生态 - 注重原始生态自然景观，如竹林、溪流、瀑布")
    NATURE_ECOLOGY("nature_ecology", "自然生态", "注重原始生态自然景观，如竹林、溪流、瀑布"),
    
    @Schema(description = "亲子出游 - 设施完善，注重天气与舒适度，距离适中")
    FAMILY_TRIP("family_trip", "亲子出游", "设施完善，注重天气与舒适度，距离适中"),
    
    @Schema(description = "打卡浏览 - 注重高人气高评分景点，用时短，快速打卡")
    QUICK_TOUR("quick_tour", "打卡浏览", "注重高人气高评分景点，用时短，快速打卡"),
    
    @Schema(description = "文化历史 - 博物馆、寺庙等文化底蕴深厚的地方")
    CULTURE_HISTORY("culture_history", "文化历史", "博物馆、寺庙等文化底蕴深厚的地方"),
    
    @Schema(description = "养生静心 - 安静舒适，林荫地带，适合冥想放松")
    WELLNESS("wellness", "养生静心", "安静舒适，林荫地带，适合冥想放松"),
    
    @Schema(description = "探索冒险 - 偏远、冷门，有索道或漂流等刺激项目")
    ADVENTURE("adventure", "探索冒险", "偏远、冷门，有索道或漂流等刺激项目"),
    
    @Schema(description = "文创体验 - 有文创体验项目或文创产品售卖的景点")
    CULTURAL_CREATIVE("cultural_creative", "文创体验", "有文创体验项目或文创产品售卖的景点");
    
    private final String code;
    private final String name;
    private final String description;
    
    TravelStrategy(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取策略
     */
    public static TravelStrategy fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (TravelStrategy strategy : values()) {
            if (strategy.code.equalsIgnoreCase(code)) {
                return strategy;
            }
        }
        return null;
    }
    
    /**
     * 根据名称获取策略
     */
    public static TravelStrategy fromName(String name) {
        if (name == null) {
            return null;
        }
        for (TravelStrategy strategy : values()) {
            if (strategy.name.equals(name)) {
                return strategy;
            }
        }
        return null;
    }
}
