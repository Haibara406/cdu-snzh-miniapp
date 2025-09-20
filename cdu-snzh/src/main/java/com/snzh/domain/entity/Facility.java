package com.snzh.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.snzh.domain.base.BaseTableData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;



/**
 * @author haibara
 * @description 基础设施表
 * @since 2025/9/20 15:37
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("facility")
@Schema(description = "基础设施实体类")
public class Facility  extends BaseTableData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "设施ID")
    private Long id;

    @Schema(description = "设施类型ID")
    private Integer facilityTypeId;

    @Schema(description = "设施名称")
    private String name;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "经度")
    private String longitude;

    @Schema(description = "纬度")
    private String latitude;

    @Schema(description = "高德地图POI ID")
    private String poiId;

    @Schema(description = "开放时间描述")
    private String openTime;

    @Schema(description = "联系电话")
    private String contactPhone;
}