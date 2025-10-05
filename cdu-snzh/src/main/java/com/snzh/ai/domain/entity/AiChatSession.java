package com.snzh.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.snzh.domain.base.BaseTableData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description AI客服会话实体类
 * @since 2025/10/5
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("ai_chat_session")
@Schema(description = "AI客服会话实体")
public class AiChatSession extends BaseTableData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "会话ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "会话唯一标识")
    private String sessionId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "会话标题（首条消息摘要）")
    private String title;
}

