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
 * @description 景点保存dto
 * @since 2025/9/27 20:24
 */
@Data
@Schema(description = "景点保存请求参数")
public class ScenicSpotSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "景点id")
    private Long id;

    @Schema(description = "父级景区ID（创建景点时可选，默认为1表示蜀南竹海）")
    private Long parentId;

    @Schema(description = "类型：0=景区（蜀南竹海）, 1=景点（默认）")
    @Min(value = 0, message = ValidationConstants.SPOT_TYPE_INCORRECT)
    @Max(value = 1, message = ValidationConstants.SPOT_TYPE_INCORRECT)
    private Integer spotType;

    @Schema(description = "景点名称")
    @NotBlank(message = ValidationConstants.SCENIC_NAME_NOT_NULL)
    @Size(min = 2, max = 100, message = ValidationConstants.SCENIC_NAME_TOO_LONG)
    private String name;

    @Schema(description = "景点描述")
    @Size(min = 2, max = 2000, message = ValidationConstants.SCENIC_DES_TOO_LONG)
    private String description;

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

    @Schema(description = "文字地址")
    @NotBlank(message = ValidationConstants.SCENIC_ADDRESS_NOT_NULL)
    @Size(max = 255, message = ValidationConstants.SCENIC_ADDRESS_TOO_LONG)
    private String address;

    @Schema(description = "3D模型链接")
    @Pattern(regexp = RegexConstant.REGEX_URL, message = ValidationConstants.INCORRECT_SCENIC_MODEL_URL)
    private String model3dUrl;

    @Schema(description = "展示顺序")
    @NotNull(message = ValidationConstants.SCENIC_DISPLAY_ORDER_NOT_NULL)
    private Integer displayOrder;

    @Schema(description = "状态（0-关闭，1-开放）")
    @Min(value = 0, message = ValidationConstants.INCORRECT_STATUS_VALUE)
    @Max(value = 1, message = ValidationConstants.INCORRECT_STATUS_VALUE)
    private Integer status;

}
