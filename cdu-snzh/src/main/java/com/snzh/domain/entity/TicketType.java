package com.snzh.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.snzh.domain.base.BaseTableData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * @author haibara
 * @description 景点票种实体类
 * @since 2025/9/20 15:34
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ticket_type")
@Schema(description = "票种实体类")
public class TicketType  extends BaseTableData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "票种ID")
    private Long id;

    @Schema(description = "票种名称")
    private String name;

    @Schema(description = "票种描述")
    private String description;

    @Schema(description = "有效天数")
    private Integer validDays;
}