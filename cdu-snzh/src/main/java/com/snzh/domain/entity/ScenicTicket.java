package com.snzh.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.snzh.domain.base.BaseTableData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;


/**
 * @author haibara
 * @description 景点票种实体类
 * @since 2025/9/20 15:34
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("scenic_ticket")
@Schema(description = "景点票种实体类")
public class ScenicTicket  extends BaseTableData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    @TableId(type = IdType.AUTO)
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
