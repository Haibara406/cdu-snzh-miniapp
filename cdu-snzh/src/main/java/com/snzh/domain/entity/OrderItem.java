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

/**
 * @author haibara
 * @description 订单明细实体类
 * @since 2025/10/2
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("order_item")
@Schema(description = "订单明细实体类")
public class OrderItem extends BaseTableData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单明细ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单项类型：1=门票, 2=住宿, 3=餐饮, 4=商品")
    private Integer itemType;

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

