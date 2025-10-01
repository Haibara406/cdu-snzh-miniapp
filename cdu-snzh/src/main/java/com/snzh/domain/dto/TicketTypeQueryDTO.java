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
 * @description 门票类型分页展示dto
 * @since 2025/9/28 10:38
 */

@Data
@Schema(description = "门票类型分页展示dto")
public class TicketTypeQueryDTO extends BasePageDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "票种名称")
    @Max(value = 100, message = ValidationConstants.TICKET_TYPE_NAME_TOO_LONG)
    private String name;

    @Schema(description = "有效天数")
    @Min(value = 1, message = ValidationConstants.VALID_DAYS_MUST_BE_POSITIVE)
    private Integer validDays;

    @Min(value = 0, message = ValidationConstants.INCORRECT_STATUS_VALUE)
    @Max(value = 1, message = ValidationConstants.INCORRECT_STATUS_VALUE)
    @Schema(description = "状态（0-关闭，1-开放）")
    private Integer status;

    @Schema(description = "创建时间开始")
    private Date createTimeStart;

    @Schema(description = "创建时间结束")
    private Date createTimeEnd;
}
