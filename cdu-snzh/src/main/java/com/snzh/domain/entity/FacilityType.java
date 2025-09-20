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
 * @description 基础设施类型
 * @since 2025/9/20 15:34
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("facility_type")
@Schema(description = "基础设施类型实体类")
public class FacilityType  extends BaseTableData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "类型ID")
    private Integer id;

    @Schema(description = "类型名称")
    private String name;
}