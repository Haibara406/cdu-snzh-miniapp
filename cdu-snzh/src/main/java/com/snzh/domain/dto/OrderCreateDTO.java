package com.snzh.domain.dto;

import com.snzh.constants.RegexConstant;
import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author haibara
 * @description 创建订单DTO
 * @since 2025/10/2
 */
@Data
@Schema(description = "创建订单请求参数")
public class OrderCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    @NotNull(message = ValidationConstants.USER_ID_NOT_NULL)
    private Long userId;

    @Schema(description = "下单人手机号")
    @Pattern(regexp = RegexConstant.REGEX_MOBILE, message = ValidationConstants.INCORRECT_PHONE_FORMAT)
    private String phone;

    @Schema(description = "订单类型：1=景点门票, 2=住宿, 3=餐饮, 4=文创商品")
    @NotNull(message = ValidationConstants.ORDER_TYPE_NOT_NULL)
    private Integer orderType;

    @Schema(description = "游玩日期")
    @NotNull(message = ValidationConstants.VISIT_DATE_NOT_NULL)
    @Future(message = ValidationConstants.VISIT_DATE_MUST_FUTURE)
    private LocalDate visitDate;

    @Schema(description = "订单明细列表")
    @NotEmpty(message = ValidationConstants.ORDER_ITEMS_NOT_EMPTY)
    @Valid
    private List<OrderItemDTO> orderItems;
}

