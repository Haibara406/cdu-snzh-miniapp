package com.snzh.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author haibara
 * @description 用户消费统计VO
 * @since 2025/10/2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户消费统计")
public class UserStatisticsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "消费TOP10用户")
    private List<TopUserVO> topUsers;

    @Schema(description = "本月新用户数")
    private Integer newUsers;

    @Schema(description = "本月活跃用户数")
    private Integer activeUsers;

    @Schema(description = "平均订单金额")
    private BigDecimal avgOrderAmount;

    /**
     * 高消费用户VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "高消费用户")
    public static class TopUserVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "手机号（脱敏）")
        private String phone;

        @Schema(description = "订单数量")
        private Integer orderCount;

        @Schema(description = "消费总金额")
        private BigDecimal totalAmount;
    }
}


