package com.snzh.domain.dto;

import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author haibara
 * @description 管理端修改订单信息DTO
 * @since 2025/10/2
 */
@Data
@Schema(description = "管理端修改订单信息")
public class AdminOrderUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "游玩日期")
    private LocalDate visitDate;

    @Schema(description = "修改原因")
    private String updateReason;
}


