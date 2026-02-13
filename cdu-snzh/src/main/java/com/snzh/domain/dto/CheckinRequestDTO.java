package com.snzh.domain.dto;

import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 签到请求DTO
 * @since 2025/02/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "签到请求DTO")
public class CheckinRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = ValidationConstants.CHECKIN_LONGITUDE_NOT_NULL)
    @DecimalMin(value = "73.66", message = ValidationConstants.LONGITUDE_EXCEEDS_CHINA)
    @DecimalMax(value = "135.05", message = ValidationConstants.LONGITUDE_EXCEEDS_CHINA)
    @Schema(description = "经度")
    private String longitude;

    @NotNull(message = ValidationConstants.CHECKIN_LATITUDE_NOT_NULL)
    @DecimalMin(value = "3.86", message = ValidationConstants.LATITUDE_EXCEEDS_CHINA)
    @DecimalMax(value = "53.55", message = ValidationConstants.LATITUDE_EXCEEDS_CHINA)
    @Schema(description = "纬度")
    private String latitude;
}
