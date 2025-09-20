package com.snzh.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import com.snzh.domain.properties.WeChatProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author haibara
 * @description 微信小程序配置类
 * @since 2025/9/20 10:02
 */
@Slf4j
@Configuration
public class WxMaConfiguration {

    /**
     * 配置WxMaService
     * @param weChatProperties 微信小程序配置属性
     * @return WxMaService实例
     */
    @Bean
    public WxMaService wxMaService(WeChatProperties weChatProperties) {
        log.info("初始化微信小程序配置: appid={}", weChatProperties.getAppid());
        
        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(weChatProperties.getAppid());
        config.setSecret(weChatProperties.getSecret());
        
        // 可以设置http客户端
        // config.setHttpClientType(HttpClientType.APACHE_HTTP);
        
        WxMaService service = new WxMaServiceImpl();
        service.setWxMaConfig(config);
        return service;
    }
} 