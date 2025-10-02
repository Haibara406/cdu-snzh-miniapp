package com.snzh.domain.dto;

import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author haibara
 * @description 管理端退款处理DTO
 * @since 2025/10/2
 */
@Data
@Schema(description = "管理端退款处理")
public class AdminRefundDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单号", required = true)
    @NotBlank(message = ValidationConstants.ORDER_NO_NOT_NULL)
    private String orderNo;

    @Schema(description = "退款金额", required = true)
    @NotNull(message = "退款金额不能为空")
    private BigDecimal refundAmount;

    @Schema(description = "退款原因")
    private String refundReason;

    @Schema(description = "备注")
    private String remark;
}


