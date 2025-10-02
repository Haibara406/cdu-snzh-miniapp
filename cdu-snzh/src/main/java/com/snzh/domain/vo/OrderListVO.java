package com.snzh.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author haibara
 * @description 订单列表VO（简化版）
 * @since 2025/10/2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单列表VO")
public class OrderListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "订单类型")
    private Integer orderType;

    @Schema(description = "订单类型描述")
    private String orderTypeDesc;

    @Schema(description = "游玩日期")
    private LocalDate visitDate;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "订单状态")
    private Integer orderStatus;

    @Schema(description = "订单状态描述")
    private String orderStatusDesc;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "订单项数量")
    private Integer itemCount;
}

