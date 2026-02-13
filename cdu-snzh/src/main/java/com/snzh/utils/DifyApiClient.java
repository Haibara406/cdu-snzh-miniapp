package com.snzh.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author haibara
 * @description Dify API 客户端，处理 SSE 流式响应
 * @since 2025/02/13
 */
@Slf4j
@Component
public class DifyApiClient {

    @Value("${dify.api.url:http://8.156.89.203:83/v1/workflows/run}")
    private String difyApiUrl;

    @Value("${dify.api.key:app-jBHSROZDAHk1EL2rM9GjLNAJ}")
    private String difyApiKey;

    /**
     * 调用 Dify API 进行地理围栏判断
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @param userId    用户ID
     * @return Dify API 响应结果
     */
    public DifyApiResponse checkGeofence(String longitude, String latitude, String userId) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, String> inputs = new HashMap<>();
            inputs.put("lan", longitude);
            inputs.put("att", latitude);
            requestBody.put("inputs", inputs);
            requestBody.put("response_mode", "streaming");
            requestBody.put("user", userId);

            String jsonBody = JSON.toJSONString(requestBody);
            log.info("调用 Dify API，请求参数: {}", jsonBody);

            // 创建连接
            URL url = new URL(difyApiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + difyApiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);

            // 发送请求
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 读取 SSE 流式响应
            int responseCode = connection.getResponseCode();
            log.info("Dify API 响应状态码: {}", responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                return parseSSEResponse(reader);
            } else {
                log.error("Dify API 调用失败，状态码: {}", responseCode);
                throw new RuntimeException("Dify API 调用失败，状态码: " + responseCode);
            }

        } catch (Exception e) {
            log.error("调用 Dify API 异常", e);
            throw new RuntimeException("调用 Dify API 异常: " + e.getMessage(), e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                log.error("关闭连接异常", e);
            }
        }
    }

    /**
     * 解析 SSE 流式响应
     */
    private DifyApiResponse parseSSEResponse(BufferedReader reader) throws Exception {
        String line;
        DifyApiResponse response = new DifyApiResponse();
        StringBuilder rawResponse = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            rawResponse.append(line).append("\n");

            if (line.startsWith("data: ")) {
                String jsonData = line.substring(6).trim();
                
                try {
                    JSONObject jsonObject = JSON.parseObject(jsonData);
                    String event = jsonObject.getString("event");

                    // 解析 node_finished 事件中的输出数据
                    if ("node_finished".equals(event)) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        if (data != null) {
                            // 检查是否是 code 类型的节点
                            String nodeType = data.getString("node_type");
                            if ("code".equals(nodeType)) {
                                JSONObject outputs = data.getJSONObject("outputs");
                                if (outputs != null) {
                                    response.setIsSign(outputs.getString("is_sign"));
                                    response.setDistance(outputs.getString("distance"));
                                    response.setSpotName(outputs.getString("spot_name"));
                                    response.setSpotId(outputs.getString("spot_id"));
                                    response.setFenceRadius(outputs.getString("fence_radius"));
                                    response.setImageUrl(outputs.getString("image_url"));
                                }
                            }
                        }
                    }

                    // 解析 workflow_finished 事件中的最终输出
                    if ("workflow_finished".equals(event)) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        if (data != null) {
                            JSONObject outputs = data.getJSONObject("outputs");
                            if (outputs != null && outputs.containsKey("img")) {
                                // 如果 workflow_finished 中有 img 字段，使用它作为最终的图片 URL
                                String finalImageUrl = outputs.getString("img");
                                if (finalImageUrl != null && !finalImageUrl.isEmpty()) {
                                    response.setImageUrl(finalImageUrl);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析 SSE 数据行失败: {}", line, e);
                }
            }
        }

        log.info("Dify API 原始响应: {}", rawResponse);
        log.info("解析后的响应: {}", response);

        return response;
    }

    /**
     * Dify API 响应数据类
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DifyApiResponse {
        private String isSign;
        private String distance;
        private String spotName;
        private String spotId;
        private String fenceRadius;
        private String imageUrl;
    }
}