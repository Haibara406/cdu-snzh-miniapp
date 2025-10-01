package com.snzh.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * @description 景区门票返回vo
 * @since 2025/9/27 19:12
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "景点门票VO")
public class ScenicTicketVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "景点ID")
    private Long scenicSpotId;

    @Schema(description = "票种ID")
    private Long ticketTypeId;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "门票售出数量")
    private Integer soldCount;
}
