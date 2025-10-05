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
 * @description AI知识库文档实体类
 * @since 2025/10/5
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("ai_knowledge_doc")
@Schema(description = "AI知识库文档实体")
public class AiKnowledgeDoc extends BaseTableData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "文档ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "文档名称")
    private String docName;

    @Schema(description = "文档类型：SCENIC=景点介绍, POLICY=政策规则, FAQ=常见问题")
    private String docType;

    @Schema(description = "文档内容（Markdown格式）")
    private String content;
}

