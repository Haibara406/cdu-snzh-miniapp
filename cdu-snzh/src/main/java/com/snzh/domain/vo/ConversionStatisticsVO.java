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
 * @description 转化率统计VO
 * @since 2025/10/2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "转化率统计")
public class ConversionStatisticsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "支付转化率(%)")
    private BigDecimal paymentRate;

    @Schema(description = "退款率(%)")
    private BigDecimal refundRate;

    @Schema(description = "取消率(%)")
    private BigDecimal cancelRate;

    @Schema(description = "完成率(%)")
    private BigDecimal completionRate;

    @Schema(description = "平均支付时长（分钟）")
    private BigDecimal avgPayTime;

    @Schema(description = "平均订单金额")
    private BigDecimal avgOrderAmount;
}


