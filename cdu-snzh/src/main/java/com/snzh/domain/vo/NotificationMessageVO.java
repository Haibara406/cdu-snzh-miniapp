package com.snzh.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author haibara
 * @description 消息通知VO
 * @since 2025/10/2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消息通知VO")
public class NotificationMessageVO {

    @Schema(description = "用户openid")
    private String openid;

    @Schema(description = "模板ID")
    private String templateId;

    @Schema(description = "跳转页面路径")
    private String page;

    @Schema(description = "消息数据")
    private Map<String, TemplateDataVO> data;

    @Schema(description = "跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版")
    private String miniprogramState;

    @Schema(description = "进入小程序查看的语言类型，支持zh_CN(简体中文)、en_US(英文)、zh_HK(繁体中文)、zh_TW(繁体中文)，默认为zh_CN")
    private String lang;

    /**
     * 模板数据项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateDataVO {
        @Schema(description = "数据值")
        private String value;
    }
}

