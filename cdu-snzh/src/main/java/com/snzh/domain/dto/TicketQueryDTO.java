package com.snzh.domain.dto;

import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author haibara
 * @description 景点门票分页搜索
 * @since 2025/9/27 20:56
 */
@Data
@Schema(description = "景点门票查询参数")
public class TicketQueryDTO extends BasePageDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "景点ID")
    private Long scenicSpotId;

    @Min(value = 0, message = ValidationConstants.INCORRECT_STATUS_VALUE)
    @Max(value = 1, message = ValidationConstants.INCORRECT_STATUS_VALUE)
    @Schema(description = "状态（0-关闭，1-开放）")
    private Integer status;

    @Schema(description = "创建时间开始")
    private Date createTimeStart;

    @Schema(description = "创建时间结束")
    private Date createTimeEnd;
}
