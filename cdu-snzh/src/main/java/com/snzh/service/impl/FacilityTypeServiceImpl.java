package com.snzh.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.snzh.constants.ErrorConst;
import com.snzh.domain.dto.FacilityTypeQueryDTO;
import com.snzh.domain.dto.FacilityTypeSaveDTO;
import com.snzh.domain.entity.FacilityType;
import com.snzh.domain.vo.FacilityTypeVO;
import com.snzh.domain.vo.PageVo;
import com.snzh.enums.RedisKeyManage;
import com.snzh.enums.StatusEnum;
import com.snzh.exceptions.FacilityTypeHasExistException;
import com.snzh.exceptions.FacilityTypeNotFoundException;
import com.snzh.mapper.FacilityTypeMapper;
import com.snzh.redis.RedisCache;
import com.snzh.redis.RedisKeyBuild;
import com.snzh.service.IFacilityTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snzh.utils.DateUtils;
import com.snzh.utils.GaodeMapUtil;
import com.snzh.utils.PageUtil;
import com.snzh.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 基础设施类型表 服务实现类
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityTypeServiceImpl extends ServiceImpl<FacilityTypeMapper, FacilityType> implements IFacilityTypeService {

    private final FacilityTypeMapper facilityTypeMapper;
    private final RedisCache redisCache;

    @Override
    public List<FacilityTypeVO> getAllEnabledTypes() {
        List<FacilityTypeVO> result = redisCache.getValueIsList(RedisKeyBuild.createKey(RedisKeyManage.FACILITY_TYPE_ALL), FacilityTypeVO.class);
        if(StringUtils.isEmpty(result)){
            LambdaQueryWrapper<FacilityType> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FacilityType::getStatus, StatusEnum.RUN.getCode())
                    .orderByAsc(FacilityType::getId);
            List<FacilityType> facilityTypes = facilityTypeMapper.selectList(wrapper);
            if(StringUtils.isNull(facilityTypes)){
                throw new FacilityTypeNotFoundException(ErrorConst.FACILITY_TYPE_NOT_FOUND);
            }
            result = facilityTypes
                    .stream()
                    .map(facilityType -> BeanUtil.copyProperties(facilityType, FacilityTypeVO.class))
                    .toList();

            redisCache.set(RedisKeyBuild.createKey(RedisKeyManage.FACILITY_TYPE_ALL), result, DateUtils.DAY_SECONDS, TimeUnit.SECONDS);
        }
        return result;

    }

    @Override
    public PageVo<FacilityTypeVO> getTypePage(FacilityTypeQueryDTO queryDTO) {
        IPage<FacilityType> page = PageUtil.getPageParams(queryDTO);
        LambdaQueryWrapper<FacilityType> wrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotNull(queryDTO)){
            wrapper.like(StringUtils.isNotEmpty(queryDTO.getName()), FacilityType::getName, queryDTO.getName())
                    .eq(StringUtils.isNotNull(queryDTO.getStatus()), FacilityType::getStatus, queryDTO.getStatus());
            if(StringUtils.isNotNull(queryDTO.getCreateTimeStart())
                    && StringUtils.isNotNull(queryDTO.getCreateTimeEnd())
                    && queryDTO.getCreateTimeStart().before(queryDTO.getCreateTimeEnd())){
                wrapper.between(FacilityType::getCreateTime, queryDTO.getCreateTimeStart(), queryDTO.getCreateTimeEnd());
            }
        }

        IPage<FacilityType> facilityTypePage = facilityTypeMapper.selectPage(page, wrapper);
        return PageUtil.convertPage(facilityTypePage, facilityType -> BeanUtil.copyProperties(facilityType, FacilityTypeVO.class));
    }

    @Override
    public FacilityTypeVO getTypeById(Long id) {
        FacilityType facilityType = redisCache.get(RedisKeyBuild.createKey(RedisKeyManage.FACILITY_TYPE_SINGLE, id), FacilityType.class);
        if(facilityType == null){
            facilityType = facilityTypeMapper.selectOne(Wrappers.lambdaQuery(FacilityType.class).eq(FacilityType::getId, id));
            if(facilityType == null){
                throw new FacilityTypeNotFoundException(ErrorConst.FACILITY_TYPE_NOT_FOUND);
            }
            redisCache.set(RedisKeyBuild.createKey(RedisKeyManage.FACILITY_TYPE_SINGLE, id), facilityType, DateUtils.DAY_SECONDS, TimeUnit.SECONDS);
        }

        return BeanUtil.copyProperties(facilityType, FacilityTypeVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addType(FacilityTypeSaveDTO saveDTO) {
        if(facilityTypeMapper.exists(Wrappers.lambdaQuery(FacilityType.class).eq(FacilityType::getName, saveDTO.getName()))){
            throw new FacilityTypeHasExistException(ErrorConst.FACILITY_TYPE_HAS_EXIST);
        }
        FacilityType facilityType = BeanUtil.copyProperties(saveDTO, FacilityType.class);
        facilityTypeMapper.insert(facilityType);

        clearCache();
        return facilityType.getId();

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateType(Long id, FacilityTypeSaveDTO saveDTO) {
        FacilityType facilityType = facilityTypeMapper.selectOne(Wrappers.lambdaQuery(FacilityType.class).eq(FacilityType::getId, id));
        if(StringUtils.isNull(facilityType)){
            throw new FacilityTypeNotFoundException(ErrorConst.FACILITY_TYPE_NOT_FOUND);
        }

        if(StringUtils.isNotNull(saveDTO)
                && !facilityType.getName().equals(saveDTO.getName())
                && facilityTypeMapper.exists(Wrappers.lambdaQuery(FacilityType.class).eq(FacilityType::getName, saveDTO.getName()))){
            throw new FacilityTypeHasExistException(ErrorConst.FACILITY_TYPE_HAS_EXIST);
        }
        facilityType = BeanUtil.copyProperties(saveDTO, FacilityType.class);
        facilityType.setId(id);
        clearCache(id);
        return updateById(facilityType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(List<Long> ids) {
        boolean result = removeByIds(ids);
        if (result) {
            // 删除缓存（单条 + ALL）
            clearCache(ids.toArray(new Long[0]));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTypeStatus(Long id, Integer status) {
        if(!facilityTypeMapper.exists(Wrappers.lambdaQuery(FacilityType.class).eq(FacilityType::getId, id))){
            throw new FacilityTypeNotFoundException(ErrorConst.FACILITY_TYPE_NOT_FOUND);
        }
        FacilityType facilityType = FacilityType.builder()
                .status(status)
                .id(id)
                .build();
        clearCache(id);
        return updateById(facilityType);
    }

    private void clearCache(Long... ids) {
        List<RedisKeyBuild> keys = new ArrayList<>();
        // 单条缓存
        if (ids != null && ids.length > 0) {
            Arrays.stream(ids).forEach(id ->
                    keys.add(RedisKeyBuild.createKey(RedisKeyManage.FACILITY_TYPE_SINGLE, id))
            );
        }
        // ALL 缓存
        keys.add(RedisKeyBuild.createKey(RedisKeyManage.FACILITY_TYPE_ALL));
        redisCache.del(keys);
    }
}
