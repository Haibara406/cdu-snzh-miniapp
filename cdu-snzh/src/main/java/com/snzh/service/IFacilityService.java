package com.snzh.service;

import cn.hutool.json.JSONObject;
import com.snzh.domain.dto.FacilityMapRequestDTO;
import com.snzh.domain.dto.FacilityQueryDTO;
import com.snzh.domain.dto.FacilitySaveDTO;
import com.snzh.domain.entity.Facility;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snzh.domain.vo.CalculateDistanceMapVO;
import com.snzh.domain.vo.NearByFacilityVO;
import com.snzh.domain.vo.FacilityVO;
import com.snzh.domain.vo.PageVo;

import java.util.List;

/**
 * <p>
 * 基础设施表 服务类
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
public interface IFacilityService extends IService<Facility> {

    List<FacilityVO> getFacilitiesByType(Long typeId);

    FacilityVO getFacilityById(Long id);

    String getFacilityMarkers(Long typeId);

    String getNavigationUrl(FacilityMapRequestDTO request);

    JSONObject getRoutePlanning(FacilityMapRequestDTO request);

    CalculateDistanceMapVO calculateDistance(FacilityMapRequestDTO request);

    List<NearByFacilityVO> findNearbyFacilities(String longitude, String latitude, Integer radius, String type, Integer page, Integer limit);

    PageVo<FacilityVO> getFacilityPage(FacilityQueryDTO queryDTO);

    Long addFacility(FacilitySaveDTO saveDTO);

    Boolean updateFacility(Long id, FacilitySaveDTO saveDTO);

    boolean deleteByIds(List<Long> ids);

    Boolean updateFacilityStatus(Long id, Integer status);
}
