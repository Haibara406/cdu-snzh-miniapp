package com.snzh.service;

import com.snzh.domain.dto.FacilityTypeQueryDTO;
import com.snzh.domain.dto.FacilityTypeSaveDTO;
import com.snzh.domain.entity.FacilityType;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snzh.domain.vo.FacilityTypeVO;
import com.snzh.domain.vo.PageVo;

import java.util.List;

/**
 * <p>
 * 基础设施类型表 服务类
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
public interface IFacilityTypeService extends IService<FacilityType> {

    List<FacilityTypeVO> getAllEnabledTypes();

    PageVo<FacilityTypeVO> getTypePage(FacilityTypeQueryDTO queryDTO);

    FacilityTypeVO getTypeById(Long id);

    Long addType(FacilityTypeSaveDTO saveDTO);

    Boolean updateType(FacilityTypeSaveDTO saveDTO);

    boolean deleteByIds(List<Long> ids);

    Boolean updateTypeStatus(Long id, Integer status);
}
