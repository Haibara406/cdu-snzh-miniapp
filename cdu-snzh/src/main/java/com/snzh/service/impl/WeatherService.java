package com.snzh.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.snzh.domain.ResponseResult;
import com.snzh.domain.vo.ForecastWeatherVO;
import com.snzh.domain.vo.LiveWeatherVO;
import com.snzh.utils.GaodeMapUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author haibara
 * @description 天气service
 * @since 2025/10/1 19:19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {
    private final GaodeMapUtil gaodeMapUtil;

    // 固定城市编码（宜宾市长宁县）
    private static final String CITY_ADCODE = "511524";

    public LiveWeatherVO getLiveWeather() {
        JSONObject result = gaodeMapUtil.queryWeather(CITY_ADCODE, "base");
        JSONArray lives = result.getJSONArray("lives");
        if (lives == null || lives.isEmpty()) {
            return null;
        }
        return JSONUtil.toBean(lives.getJSONObject(0), LiveWeatherVO.class);
    }

    public ForecastWeatherVO getForecastWeather() {
        JSONObject result = gaodeMapUtil.queryWeather(CITY_ADCODE, "all");
        JSONArray forecasts = result.getJSONArray("forecasts");
        if (forecasts == null || forecasts.isEmpty()) {
            return null;
        }
        return JSONUtil.toBean(forecasts.getJSONObject(0), ForecastWeatherVO.class);
    }
}
