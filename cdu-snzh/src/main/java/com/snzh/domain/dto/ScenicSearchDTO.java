package com.snzh.domain.dto;

import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 景区搜索dto
 * @since 2025/9/27 20:18
 */
@Data
@Schema(description = "景区搜索dto")
public class ScenicSearchDTO extends BasePageDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "关键词")
    @Max(value = 50, message = ValidationConstants.SCENIC_KEYWORD_TOO_LONG)
    private String keyword;

}
