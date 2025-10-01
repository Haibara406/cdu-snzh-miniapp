package com.snzh.domain.vo;

import lombok.Data;

/**
 * @author haibara
 * @description 实时天气vo
 * @since 2025/10/1 19:23
 */

@Data
public class LiveWeatherVO {
    private String province;       // 省份
    private String city;           // 城市
    private String adcode;         // 区域编码
    private String weather;        // 天气现象
    private String temperature;    // 温度
    private String winddirection;  // 风向
    private String windpower;      // 风力
    private String humidity;       // 湿度
    private String reporttime;     // 数据发布时间
}
