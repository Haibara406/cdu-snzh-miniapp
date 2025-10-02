package com.snzh.domain.dto;

import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 取消订单DTO
 * @since 2025/10/2
 */
@Data
@Schema(description = "取消订单请求参数")
public class OrderCancelDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    @NotNull(message = ValidationConstants.USER_ID_NOT_NULL)
    private Long userId;

    @Schema(description = "订单号")
    @NotBlank(message = ValidationConstants.ORDER_NO_NOT_NULL)
    private String orderNo;

    @Schema(description = "取消原因")
    @Size(max = 255, message = ValidationConstants.CANCEL_REASON_TOO_LONG)
    private String cancelReason;
}

