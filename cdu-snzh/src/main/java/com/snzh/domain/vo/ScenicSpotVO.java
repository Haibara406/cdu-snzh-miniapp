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
import java.util.List;

/**
 * @author haibara
 * @description 景区详情vo
 * @since 2025/9/27 19:08
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "景点VO")
public class ScenicSpotVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "景点ID")
    private Long id;

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
    private String model3dUrl;

    @Schema(description = "展示顺序")
    private Integer displayOrder;

    private List<ScenicImageVO> images;

    private List<ScenicTicketVO> tickets;
}
