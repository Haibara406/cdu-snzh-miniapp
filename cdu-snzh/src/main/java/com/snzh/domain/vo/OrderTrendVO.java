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
 * @description 订单趋势统计VO
 * @since 2025/10/2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单趋势统计")
public class OrderTrendVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "日期")
    private String date;

    @Schema(description = "订单数量")
    private Integer orderCount;

    @Schema(description = "销售额")
    private BigDecimal revenue;

    @Schema(description = "已支付订单数")
    private Integer paidCount;
}


