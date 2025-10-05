package com.snzh.ai.domain.dto;

import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 知识库文档保存DTO
 * @since 2025/10/5
 */
@Data
@Schema(description = "知识库文档保存参数")
public class KnowledgeDocSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "文档ID（新增时为空，修改时必填）")
    private Long id;

    @Schema(description = "文档名称")
    @NotBlank(message = ValidationConstants.KNOWLEDGE_DOC_NAME_NOT_NULL)
    @Size(max = 255, message = ValidationConstants.KNOWLEDGE_DOC_NAME_TOO_LONG)
    private String docName;

    @Schema(description = "文档类型：SCENIC=景点介绍, POLICY=政策规则, FAQ=常见问题")
    @NotBlank(message = ValidationConstants.KNOWLEDGE_DOC_TYPE_NOT_NULL)
    private String docType;

    @Schema(description = "文档内容（Markdown格式）")
    @NotBlank(message = ValidationConstants.KNOWLEDGE_DOC_CONTENT_NOT_NULL)
    private String content;

    @Schema(description = "状态：0=禁用, 1=启用")
    private Integer status;
}

