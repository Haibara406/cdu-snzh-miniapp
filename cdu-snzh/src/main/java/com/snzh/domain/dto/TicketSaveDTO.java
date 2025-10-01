package com.snzh.domain.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.snzh.constants.RegexConstant;
import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author haibara
 * @description 门票保存dto
 * @since 2025/9/28 10:12
 */
@Data
@Schema(description = "门票保存dto")
public class TicketSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "门票id")
    private Long id;

    @Schema(description = "景点id")
    @NotNull(message = ValidationConstants.SCENIC_ID_NOT_NULL)
    private Long scenicSpotId;

    @Schema(description = "票种id")
    @NotNull(message = ValidationConstants.TICKET_TYPE_ID_NOT_NULL)
    private Long ticketTypeId;

    @Schema(description = "价格")
    @Pattern(regexp = RegexConstant.REGEX_MONEY, message = ValidationConstants.INCORRECT_PRICE)
    private BigDecimal price;

    @Schema(description = "门票售出数量")
    @Min(value = 0, message = ValidationConstants.SOLD_COUNT_MUST_BE_POSITIVE)
    private Integer soldCount;

    @Schema(description = "状态:0-禁用,1-正常")
    @Min(value = 0, message = ValidationConstants.INCORRECT_STATUS_VALUE)
    @Max(value = 1, message = ValidationConstants.INCORRECT_STATUS_VALUE)
    private Integer status;
}
