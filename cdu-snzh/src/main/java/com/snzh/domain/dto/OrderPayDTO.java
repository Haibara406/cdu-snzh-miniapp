package com.snzh.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author haibara
 * @description 支付订单DTO
 * @since 2025/10/21
 */
@Data
@Schema(description = "支付订单请求参数")
public class OrderPayDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单号")
    @NotBlank(message = "订单号不能为空")
    private String orderNo;
}


