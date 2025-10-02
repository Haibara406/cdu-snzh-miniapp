package com.snzh.domain.dto;

import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author haibara
 * @description 订单明细项DTO
 * @since 2025/10/2
 */
@Data
@Schema(description = "订单明细项DTO")
public class OrderItemDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单项类型：1=门票, 2=住宿, 3=餐饮, 4=商品")
    @NotNull(message = ValidationConstants.ORDER_ITEM_TYPE_NOT_NULL)
    private Integer itemType;

    @Schema(description = "对应商品ID（如scenic_ticket.id）")
    @NotNull(message = ValidationConstants.ORDER_ITEM_ID_NOT_NULL)
    private Long itemId;

    @Schema(description = "商品名称")
    @NotBlank(message = ValidationConstants.ORDER_ITEM_NAME_NOT_NULL)
    private String itemName;

    @Schema(description = "数量")
    @NotNull(message = ValidationConstants.ORDER_QUANTITY_NOT_NULL)
    @Min(value = 1, message = ValidationConstants.ORDER_QUANTITY_MIN)
    private Integer quantity;

    @Schema(description = "单价")
    @NotNull(message = ValidationConstants.ORDER_PRICE_NOT_NULL)
    private BigDecimal price;
}

