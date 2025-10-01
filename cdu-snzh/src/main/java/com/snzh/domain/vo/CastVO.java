package com.snzh.domain.vo;

import lombok.Data;

/**
 * @author haibara
 * @description 单日预报
 * @since 2025/10/1 19:23
 */
@Data
public class CastVO {

    private String date;          // 日期
    private String week;          // 星期几
    private String dayweather;    // 白天天气
    private String nightweather;  // 晚上天气
    private String daytemp;       // 白天温度
    private String nighttemp;     // 夜间温度
    private String daywind;       // 白天风向
    private String nightwind;     // 晚上风向
    private String daypower;      // 白天风力
    private String nightpower;    // 晚上风力
}
