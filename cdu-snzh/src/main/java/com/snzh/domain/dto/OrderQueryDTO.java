package com.snzh.domain.dto;

import com.snzh.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * @author haibara
 * @description 订单查询DTO
 * @since 2025/10/2
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订单查询请求参数")
public class OrderQueryDTO extends BasePageDto {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    @NotNull(message = ValidationConstants.USER_ID_NOT_NULL)
    private Long userId;

    @Schema(description = "订单状态：0=待支付, 1=已支付, 2=已取消")
    private Integer orderStatus;

    @Schema(description = "订单类型：1=景点门票, 2=住宿, 3=餐饮, 4=文创商品")
    private Integer orderType;

    @Schema(description = "创建时间开始")
    private Date createTimeStart;

    @Schema(description = "创建时间结束")
    private Date createTimeEnd;
}

