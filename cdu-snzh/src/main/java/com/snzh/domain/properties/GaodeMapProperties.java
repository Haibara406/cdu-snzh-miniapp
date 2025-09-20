package com.snzh.domain.properties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Haibara
 * @project backend
 * @description
 * @since 2025-07-02 14:37
 */

@Component
@ConfigurationProperties(prefix = "snzh.gaode")
@Data
public class GaodeMapProperties {
    /** 高德API密钥 */
    private String key;

    /** 高德API地址前缀 */
    private String address;

    /** 请求超时时间（毫秒） */
    private Integer timeout;

    /** 连接超时时间（毫秒） */
    private Integer connectTimeout;

    /** 静态地图相关配置 */
    private StaticMap staticMap = new StaticMap();

    /** 静态地图配置内部类 */
    @Setter
    @Getter
    public static class StaticMap {
        private Integer zoom;
        private Integer width;
        private Integer height;

    }
}
