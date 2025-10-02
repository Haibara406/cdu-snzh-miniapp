package com.snzh.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author haibara
 * @description 订单数据看板VO
 * @since 2025/10/2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单数据看板")
public class OrderDashboardVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ========== 今日数据 ==========
    @Schema(description = "今日订单总数")
    private Integer todayTotal;

    @Schema(description = "今日待支付订单数")
    private Integer todayPending;

    @Schema(description = "今日已支付订单数")
    private Integer todayPaid;

    @Schema(description = "今日已取消订单数")
    private Integer todayCancelled;

    @Schema(description = "今日已退款订单数")
    private Integer todayRefunded;

    @Schema(description = "今日已完成订单数")
    private Integer todayCompleted;

    @Schema(description = "今日销售额（已支付+已完成）")
    private BigDecimal todayRevenue;

    // ========== 本月数据 ==========
    @Schema(description = "本月订单总数")
    private Integer monthTotal;

    @Schema(description = "本月待支付订单数")
    private Integer monthPending;

    @Schema(description = "本月已支付订单数")
    private Integer monthPaid;

    @Schema(description = "本月已取消订单数")
    private Integer monthCancelled;

    @Schema(description = "本月已退款订单数")
    private Integer monthRefunded;

    @Schema(description = "本月已完成订单数")
    private Integer monthCompleted;

    @Schema(description = "本月销售额（已支付+已完成）")
    private BigDecimal monthRevenue;

    // ========== 特殊提醒 ==========
    @Schema(description = "异常订单数（超时未支付的待支付订单）")
    private Integer abnormalOrders;
}


