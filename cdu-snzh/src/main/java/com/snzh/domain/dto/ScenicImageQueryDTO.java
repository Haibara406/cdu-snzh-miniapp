package com.snzh.domain.dto;

import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author haibara
 * @description 景点图片分页查询dto
 * @since 2025/9/28 11:10
 */

@Data
@Schema(description = "景点图片分页查询dto")
public class ScenicImageQueryDTO extends BasePageDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "景点id")
    private Integer scenicId;

    @Min(value = 0, message = ValidationConstants.INCORRECT_STATUS_VALUE)
    @Max(value = 1, message = ValidationConstants.INCORRECT_STATUS_VALUE)
    @Schema(description = "状态（0-禁用，1-启用）")
    private Integer status;

    @Schema(description = "创建时间开始")
    private Date createTimeStart;

    @Schema(description = "创建时间结束")
    private Date createTimeEnd;
}
