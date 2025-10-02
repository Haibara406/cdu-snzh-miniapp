package com.snzh.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.snzh.domain.base.BaseTableData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author haibara
 * @description 订单实体类
 * @since 2025/10/2
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("orders")
@Schema(description = "订单实体类")
public class Order extends BaseTableData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "下单人手机号")
    private String phone;

    @Schema(description = "订单类型：1=景点门票, 2=住宿, 3=餐饮, 4=文创商品")
    private Integer orderType;

    @Schema(description = "游玩日期")
    private LocalDate visitDate;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "订单状态：0=待支付, 1=已支付, 2=已取消, 3=已退款, 4=已完成")
    private Integer orderStatus;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "取消时间")
    private LocalDateTime cancelTime;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "退款时间")
    private LocalDateTime refundTime;

    @Schema(description = "完成时间")
    private LocalDateTime completeTime;

    @Schema(description = "订单过期时间")
    private LocalDateTime expireTime;
}

