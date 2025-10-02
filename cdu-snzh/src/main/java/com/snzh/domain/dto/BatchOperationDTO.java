package com.snzh.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author haibara
 * @description 批量操作DTO
 * @since 2025/10/2
 */
@Data
@Schema(description = "批量操作请求")
public class BatchOperationDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID列表", required = true)
    @NotEmpty(message = "订单ID列表不能为空")
    private List<Long> orderIds;

    @Schema(description = "操作类型：cancel-取消, refund-退款, complete-完成", required = true)
    @NotBlank(message = "操作类型不能为空")
    private String operation;

    @Schema(description = "操作原因")
    private String reason;
}


