package com.snzh.domain.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author haibara
 * @description 门票类型返回vo
 * @since 2025/9/28 10:35
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "门票类型返回vo")
public class TicketTypeVO implements Serializable {

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

    @Schema(description = "状态:0-禁用,1-正常")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
