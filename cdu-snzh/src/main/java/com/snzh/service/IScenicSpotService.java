package com.snzh.service;

import cn.hutool.json.JSONObject;
import com.snzh.domain.dto.ScenicQueryDTO;
import com.snzh.domain.dto.ScenicSpotMapRequestDTO;
import com.snzh.domain.dto.ScenicSpotSaveDTO;
import com.snzh.domain.entity.ScenicSpot;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snzh.domain.vo.CalculateDistanceMapVO;
import com.snzh.domain.vo.NearByScenicSpotVO;
import com.snzh.domain.vo.PageVo;
import com.snzh.domain.vo.ScenicSpotVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * <p>
 * 景点表 服务类
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
public interface IScenicSpotService extends IService<ScenicSpot> {

    ScenicSpotVO getScenicSpotDetail(Long id);

    List<ScenicSpotVO> getScenicSpotList();

    String getScenicMarkers();

    String getNavigationUrl(@Valid ScenicSpotMapRequestDTO dto);

    JSONObject getRoutePlanning(@Valid ScenicSpotMapRequestDTO dto);

    CalculateDistanceMapVO calculateDistance(@Valid ScenicSpotMapRequestDTO dto);

    List<NearByScenicSpotVO> findNearbySpots(@NotNull String longitude, @NotNull String latitude, Integer radius, @NotNull String type, Integer page, Integer limit);

    String getScenicModel3dUrl(Long scenicSpotId);

    PageVo<ScenicSpotVO> getScenicPage(@Valid ScenicQueryDTO dto);

    Long addScenicSpot(@Valid ScenicSpotSaveDTO dto);

    Boolean updateScenicSpot(@Valid ScenicSpotSaveDTO dto);

    Boolean deleteByIds(List<Long> ids);

    Boolean updateScenicSpotStatus(Long id, Integer status);

    Boolean updateScenicArea(@Valid ScenicSpotSaveDTO dto);
}
