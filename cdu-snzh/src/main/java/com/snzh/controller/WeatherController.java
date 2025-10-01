package com.snzh.controller;

import com.snzh.domain.ResponseResult;
import com.snzh.domain.vo.ForecastWeatherVO;
import com.snzh.domain.vo.LiveWeatherVO;
import com.snzh.service.impl.WeatherService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author haibara
 * @description 天气控制器
 * @since 2025/10/1 19:17
 */
@Slf4j
@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
@Tag(name = "天气接口", description = "天气相关接口")
public class WeatherController {

    private final WeatherService weatherService;

    /**
     * 实时天气（固定查询宜宾市筠连县）
     */
    @GetMapping("/live")
    public ResponseResult<LiveWeatherVO> getLiveWeather() {
        return ResponseResult.success(weatherService.getLiveWeather());
    }

    /**
     * 预报天气（固定查询宜宾市筠连县）
     */
    @GetMapping("/forecast")
    public ResponseResult<ForecastWeatherVO> getForecastWeather() {
        return ResponseResult.success(weatherService.getForecastWeather());
    }
}
