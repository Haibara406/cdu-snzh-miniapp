package com.snzh.domain.dto;

import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 基础设施地图相关 dto
 * @since 2025/9/21 19:39
 */

@Data
@Schema(description = "设施地图请求参数")
public class FacilityMapRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户当前位置经度")
    @DecimalMin(value = "73.66", message = "经度超出中国范围")
    @DecimalMax(value = "135.05", message = "经度超出中国范围")
    private String userLongitude;

    @Schema(description = "用户当前位置纬度")
    @DecimalMin(value = "3.86", message = "纬度超出中国范围")
    @DecimalMax(value = "53.55", message = "纬度超出中国范围")
    private String userLatitude;

    @NotNull(message = ValidationConstants.FACILITY_ID_NOT_NULL)
    @Schema(description = "设施ID")
    private Long facilityId;

    @Min(value = 0, message = ValidationConstants.INCORRECT_NAV_TYPE)
    @Max(value = 4, message = ValidationConstants.INCORRECT_NAV_TYPE)
    @Schema(description = "导航类型：0-驾车，1-步行，2-公交，3-骑行，4-AR步行")
    private Integer navType;

    @Min(value = 0, message = ValidationConstants.INCORRECT_DISTANCE_TYPE)
    @Max(value = 2, message = ValidationConstants.INCORRECT_DISTANCE_TYPE)
    @Schema(description = "距离计算类型：0-直线距离，1-驾车距离，2-步行距离")
    private Integer distanceType;
}
