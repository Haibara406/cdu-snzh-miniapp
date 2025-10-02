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
 * @description 订单明细VO
 * @since 2025/10/2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单明细VO")
public class OrderItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单明细ID")
    private Long id;

    @Schema(description = "订单项类型：1=门票, 2=住宿, 3=餐饮, 4=商品")
    private Integer itemType;

    @Schema(description = "订单项类型描述")
    private String itemTypeDesc;

    @Schema(description = "对应商品ID")
    private Long itemId;

    @Schema(description = "商品名称")
    private String itemName;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "单价")
    private BigDecimal price;

    @Schema(description = "小计")
    private BigDecimal totalAmount;
}

