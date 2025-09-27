package com.snzh.domain.dto;

import com.snzh.constants.RegexConstant;
import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 新增基础设施 dto
 * @since 2025/9/21 19:48
 */

@Data
@Schema(description = "设施保存请求参数")
public class FacilitySaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = ValidationConstants.FACILITY_TYPE_ID_NOT_NULL)
    @Schema(description = "设施类型ID")
    private Integer facilityTypeId;

    @NotBlank(message = ValidationConstants.FACILITY_NAME_NOT_NULL)
    @Size(min = 2, max = 100, message = ValidationConstants.FACILITY_NAME_TOO_LONG)
    @Schema(description = "设施名称")
    private String name;

    @NotBlank(message = ValidationConstants.FACILITY_ADDRESS_NOT_NULL)
    @Size(max = 255, message = ValidationConstants.FACILITY_ADDRESS_TOO_LONG)
    @Schema(description = "详细地址")
    private String address;

    @NotNull(message = ValidationConstants.LONGITUDE_NOT_NULL)
    @DecimalMin(value = "73.66", message = ValidationConstants.LONGITUDE_EXCEEDS_CHINA)
    @DecimalMax(value = "135.05", message = ValidationConstants.LONGITUDE_EXCEEDS_CHINA)
    @Schema(description = "经度")
    private String longitude;

    @NotNull(message = ValidationConstants.LATITUDE_NOT_NULL)
    @DecimalMin(value = "3.86", message = ValidationConstants.LATITUDE_EXCEEDS_CHINA)
    @DecimalMax(value = "53.55", message = ValidationConstants.LATITUDE_EXCEEDS_CHINA)
    @Schema(description = "纬度")
    private String latitude;

    @Schema(description = "高德地图POI ID")
    private String poiId;

    @Schema(description = "状态（0-关闭，1-开放）")
    @Min(value = 0, message = ValidationConstants.INCORRECT_STATUS_VALUE)
    @Max(value = 1, message = ValidationConstants.INCORRECT_STATUS_VALUE)
    private Integer status;

    @Size(max = 100, message = ValidationConstants.FACILITY_OPEN_TIME_DESCRIBE_TOO_LONG)
    @Schema(description = "开放时间描述")
    private String openTime;

    @Pattern(regexp = RegexConstant.REGEX_MOBILE, message = ValidationConstants.INCORRECT_PHONE_FORMAT)
    @Schema(description = "联系电话")
    private String contactPhone;
}
