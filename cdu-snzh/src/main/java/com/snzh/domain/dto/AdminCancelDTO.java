package com.snzh.domain.dto;

import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 管理端取消订单DTO
 * @since 2025/10/2
 */
@Data
@Schema(description = "管理端取消订单请求")
public class AdminCancelDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单号", required = true)
    @NotBlank(message = ValidationConstants.ORDER_NO_NOT_NULL)
    private String orderNo;

    @Schema(description = "取消原因")
    private String cancelReason;
}


