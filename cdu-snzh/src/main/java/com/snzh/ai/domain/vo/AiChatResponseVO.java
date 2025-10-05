package com.snzh.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author haibara
 * @description AI对话响应VO
 * @since 2025/10/5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI对话响应")
public class AiChatResponseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "AI回复内容")
    private String reply;

    @Schema(description = "回复时间")
    private LocalDateTime timestamp;

    @Schema(description = "是否是新会话")
    private Boolean isNewSession;
}

