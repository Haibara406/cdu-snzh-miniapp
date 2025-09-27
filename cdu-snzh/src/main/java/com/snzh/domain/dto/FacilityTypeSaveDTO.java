package com.snzh.domain.dto;

import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 新增基础设施 dto
 * @since 2025/9/21 14:46
 */

@Data
@Schema(description = "设施类型保存参数")
public class FacilityTypeSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = ValidationConstants.FACILITY_TYPE_NAME_NOT_NULL)
    @Size(max = 50, message = ValidationConstants.FACILITY_TYPE_NAME_TOO_LONG)
    @Schema(description = "类型名称")
    private String name;

    @Min(value = 0, message = ValidationConstants.INCORRECT_STATUS_VALUE)
    @Max(value = 1, message = ValidationConstants.INCORRECT_STATUS_VALUE)
    @Schema(description = "状态（0-禁用，1-启用）", defaultValue = "1")
    private Integer status = 1;

}
