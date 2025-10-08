package com.snzh.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;

import com.snzh.domain.base.BaseTableData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


/**
 * @author haibara
 * @description 景点实体类
 * @since 2025/9/20 15:34
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("scenic_spot")
@Schema(description = "景点实体类")
public class ScenicSpot  extends BaseTableData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "景点ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "父级景区ID（NULL或1表示蜀南竹海，景点默认为1）")
    private Long parentId;

    @Schema(description = "类型：0=景区（蜀南竹海）, 1=景点（默认）")
    private Integer spotType;

    @Schema(description = "景点名称")
    private String name;

    @Schema(description = "景点描述")
    private String description;

    @Schema(description = "经度")
    private String longitude;

    @Schema(description = "纬度")
    private String latitude;

    @Schema(description = "高德地图POI ID")
    private String poiId;

    @Schema(description = "文字地址")
    private String address;

    @Schema(description = "3D模型链接")
    @TableField("model_3d_url")
    private String model3dUrl;

    @Schema(description = "展示顺序")
    private Integer displayOrder;
}