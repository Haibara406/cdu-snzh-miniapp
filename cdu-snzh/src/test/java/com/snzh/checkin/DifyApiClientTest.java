package com.snzh.checkin;


import com.snzh.utils.DifyApiClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author haibara
 * @description Dify API 客户端测试
 * @since 2025/02/13
 */
@Slf4j
@SpringBootTest
public class DifyApiClientTest {

    @Autowired
    private DifyApiClient difyApiClient;

    @Test
    public void testCheckGeofenceSuccess() {
        // 测试在景点围栏内的坐标
        String longitude = "104.985586";
        String latitude = "28.451742";
        String userId = "test-user-123";

        log.info("测试签到成功场景");
        DifyApiClient.DifyApiResponse response = difyApiClient.checkGeofence(longitude, latitude, userId);

        log.info("响应结果: {}", response);
        log.info("是否签到成功: {}", response.getIsSign());
        log.info("景点名称: {}", response.getSpotName());
        log.info("景点ID: {}", response.getSpotId());
        log.info("距离: {} 米", response.getDistance());
        log.info("勋章图片: {}", response.getImageUrl());
    }

    @Test
    public void testCheckGeofenceFail() {
        // 测试不在景点围栏内的坐标
        String longitude = "104.000000";
        String latitude = "28.000000";
        String userId = "test-user-123";

        log.info("测试签到失败场景");
        DifyApiClient.DifyApiResponse response = difyApiClient.checkGeofence(longitude, latitude, userId);

        log.info("响应结果: {}", response);
        log.info("是否签到成功: {}", response.getIsSign());
        log.info("景点名称: {}", response.getSpotName());
        log.info("距离: {} 米", response.getDistance());
    }
}