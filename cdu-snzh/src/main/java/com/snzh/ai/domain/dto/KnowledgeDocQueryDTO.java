package com.snzh.ai.domain.dto;

import com.snzh.domain.dto.BasePageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * @author haibara
 * @description 知识库文档查询DTO
 * @since 2025/10/5
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "知识库文档查询参数")
public class KnowledgeDocQueryDTO extends BasePageDto {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "文档名称（模糊查询）")
    private String docName;

    @Schema(description = "文档类型：SCENIC=景点介绍, POLICY=政策规则, FAQ=常见问题")
    private String docType;

    @Schema(description = "状态：0=禁用, 1=启用")
    private Integer status;

    @Schema(description = "创建时间起始")
    private Date createTimeStart;

    @Schema(description = "创建时间结束")
    private Date createTimeEnd;
}

