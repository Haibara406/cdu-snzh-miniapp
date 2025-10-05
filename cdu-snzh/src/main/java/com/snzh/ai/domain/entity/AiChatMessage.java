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
 * @description AI客服消息记录实体类
 * @since 2025/10/5
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("ai_chat_message")
@Schema(description = "AI客服消息记录实体")
public class AiChatMessage extends BaseTableData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "消息ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "角色：USER=用户, AI=助手, SYSTEM=系统")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "元数据（工具调用记录等）")
    private String metadata;
}

