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

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author haibara
 * @description 景点图片返回
 * @since 2025/9/27 14:35
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "景点图片VO")
public class ScenicImageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "图片ID")
    private Long id;

    @Schema(description = "关联景点ID")
    private Long scenicId;

    @Schema(description = "图片地址")
    private String imgUrl;

    @Schema(description = "图片类型：0=封面,1=详情,2=其他")
    private Integer type;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "照片名称")
    private String name;

    @Schema(description = "照片体积大小(KB)")
    private Double size;

    @Schema(description = "状态:0-禁用,1-正常")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
