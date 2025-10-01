package com.snzh.domain.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 票种类型保存dto
 * @since 2025/9/28 10:44
 */

@Data
@Schema(description = "票种类型保存dto")
public class TicketTypeSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "票种ID")
    private Long id;

    @Schema(description = "票种名称")
    @Max(value = 100, message = ValidationConstants.TICKET_TYPE_NAME_TOO_LONG)
    @NotBlank(message = ValidationConstants.TICKET_TYPE_NAME_NOT_NULL)
    private String name;

    @Schema(description = "票种描述")
    @Size(min = 2, max = 2000, message = ValidationConstants.TICKET_TYPE_DES_TOO_LONG)
    private String description;

    @Schema(description = "有效天数")
    @Min(value = 1, message = ValidationConstants.VALID_DAYS_MUST_BE_POSITIVE)
    private Integer validDays;

    @Schema(description = "状态:0-禁用,1-正常")
    @Min(value = 0, message = ValidationConstants.INCORRECT_STATUS_VALUE)
    @Max(value = 1, message = ValidationConstants.INCORRECT_STATUS_VALUE)
    private Integer status;
}
