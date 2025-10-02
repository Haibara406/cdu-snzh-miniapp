package com.snzh.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author haibara
 * @description 管理端订单查询DTO
 * @since 2025/10/2
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理端订单查询参数")
public class AdminOrderQueryDTO extends BasePageDto {

    @Schema(description = "订单号（模糊搜索）")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "手机号（模糊搜索）")
    private String phone;

    @Schema(description = "订单状态：0=待支付, 1=已支付, 2=已取消, 3=已退款, 4=已完成")
    private Integer orderStatus;

    @Schema(description = "订单类型：1=景点门票, 2=住宿, 3=餐饮, 4=文创商品")
    private Integer orderType;

    @Schema(description = "下单开始时间")
    private LocalDateTime createTimeStart;

    @Schema(description = "下单结束时间")
    private LocalDateTime createTimeEnd;

    @Schema(description = "游玩开始日期")
    private LocalDate visitDateStart;

    @Schema(description = "游玩结束日期")
    private LocalDate visitDateEnd;

    @Schema(description = "最小金额")
    private BigDecimal minAmount;

    @Schema(description = "最大金额")
    private BigDecimal maxAmount;

    @Schema(description = "快捷筛选：today-今日, pending-待支付, refund-待退款, abnormal-异常")
    private String quickFilter;
}


