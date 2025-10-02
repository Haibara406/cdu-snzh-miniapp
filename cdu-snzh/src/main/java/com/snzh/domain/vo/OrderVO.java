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
import java.util.List;

/**
 * @author haibara
 * @description 订单详情VO
 * @since 2025/10/2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单详情VO")
public class OrderVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "下单人手机号")
    private String phone;

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

    @Schema(description = "取消时间")
    private LocalDateTime cancelTime;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "退款时间")
    private LocalDateTime refundTime;

    @Schema(description = "订单过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "订单明细列表")
    private List<OrderItemVO> orderItems;
}

