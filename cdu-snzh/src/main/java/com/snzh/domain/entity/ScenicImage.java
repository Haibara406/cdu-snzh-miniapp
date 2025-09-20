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
 * @description 景点图片实体类
 * @since 2025/9/20 15:34
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("scenic_image")
@Schema(description = "景点图片实体类")
public class ScenicImage  extends BaseTableData implements Serializable {

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
}
