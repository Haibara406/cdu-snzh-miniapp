package com.snzh.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 快捷筛选VO
 * @since 2025/10/2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "快捷筛选数据")
public class QuickFilterVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "待支付订单数（全部）")
    private Integer pendingOrders;

    @Schema(description = "已支付订单数（全部，未完成的）")
    private Integer paidOrders;

    @Schema(description = "已退款订单数（全部）")
    private Integer refundedOrders;

    @Schema(description = "已取消订单数（全部）")
    private Integer cancelledOrders;

    @Schema(description = "已完成订单数（全部）")
    private Integer completedOrders;

    @Schema(description = "异常订单数（超时未支付）")
    private Integer abnormalOrders;
}


