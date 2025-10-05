package com.snzh.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description AI对话请求DTO
 * @since 2025/10/5
 */
@Data
@Schema(description = "AI对话请求参数")
public class AiChatRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "会话ID（首次对话可为空）")
    private String sessionId;

    @Schema(description = "用户消息")
    @NotBlank(message = "消息内容不能为空")
    private String message;

    @Schema(description = "用户ID")
    @NotNull(message = "用户ID不能为空")
    private Long userId;
}

