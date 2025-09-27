package com.snzh.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author haibara
 * @description 基础设施查询 dto
 * @since 2025/9/21 19:45
 */

@Data
@Schema(description = "设施查询参数")
public class FacilityQueryDTO extends BasePageDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Size(max = 100, message = "设施名称不能超过100个字符")
    @Schema(description = "设施名称（模糊查询）")
    private String name;

    @Schema(description = "设施类型ID")
    private Integer facilityTypeId;

    @Min(value = 0, message = "状态值不正确")
    @Max(value = 1, message = "状态值不正确")
    @Schema(description = "状态（0-关闭，1-开放）")
    private Integer status;

    @Schema(description = "地址关键词（模糊查询）")
    @Size(max = 50, message = "地址关键词不能超过50个字符")
    private String addressKeyword;

    @Schema(description = "创建时间开始")
    private Date createTimeStart;

    @Schema(description = "创建时间结束")
    private Date createTimeEnd;
}
