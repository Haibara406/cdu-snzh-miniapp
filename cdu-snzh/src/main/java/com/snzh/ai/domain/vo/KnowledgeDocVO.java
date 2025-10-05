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
 * @description 知识库文档VO
 * @since 2025/10/5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "知识库文档信息")
public class KnowledgeDocVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "文档ID")
    private Long id;

    @Schema(description = "文档名称")
    private String docName;

    @Schema(description = "文档类型：SCENIC=景点介绍, POLICY=政策规则, FAQ=常见问题")
    private String docType;

    @Schema(description = "文档内容（Markdown格式）")
    private String content;

    @Schema(description = "文档内容长度（字符数）")
    private Integer contentLength;

    @Schema(description = "状态：0=禁用, 1=启用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

