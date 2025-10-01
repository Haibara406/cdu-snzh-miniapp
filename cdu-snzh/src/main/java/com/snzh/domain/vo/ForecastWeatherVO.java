package com.snzh.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * @author haibara
 * @description 预报天气
 * @since 2025/10/1 19:23
 */
@Data
public class ForecastWeatherVO {

    private String city;              // 城市
    private String adcode;            // 城市编码
    private String province;          // 省份
    private String reporttime;        // 发布时间
    private List<CastVO> casts;      // 天气预报
}
