package com.snzh.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.snzh.constants.ErrorConst;
import com.snzh.constants.MapConstant;
import com.snzh.domain.dto.FacilityMapRequestDTO;
import com.snzh.domain.dto.FacilityQueryDTO;
import com.snzh.domain.dto.FacilitySaveDTO;
import com.snzh.domain.entity.Facility;
import com.snzh.domain.entity.FacilityType;
import com.snzh.domain.vo.CalculateDistanceMapVO;
import com.snzh.domain.vo.NearByFacilityVO;
import com.snzh.domain.vo.FacilityVO;
import com.snzh.domain.vo.PageVo;
import com.snzh.enums.RedisKeyManage;
import com.snzh.enums.StatusEnum;
import com.snzh.exceptions.*;
import com.snzh.mapper.FacilityMapper;
import com.snzh.mapper.FacilityTypeMapper;
import com.snzh.redis.RedisCache;
import com.snzh.redis.RedisKeyBuild;
import com.snzh.service.IFacilityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snzh.utils.DateUtils;
import com.snzh.utils.GaodeMapUtil;
import com.snzh.utils.PageUtil;
import com.snzh.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 基础设施表 服务实现类
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityServiceImpl extends ServiceImpl<FacilityMapper, Facility> implements IFacilityService {

    private final FacilityMapper facilityMapper;
    private final RedisCache redisCache;
    private final GaodeMapUtil gaodeMapUtil;
    private final FacilityTypeMapper facilityTypeMapper;


    @Override
    public List<FacilityVO> getFacilitiesByType(Long typeId) {
        List<FacilityVO> facilities = redisCache.getValueIsList(RedisKeyBuild.createKey(RedisKeyManage.FACILITY_FOR_TYPE, typeId), FacilityVO.class);
        if(StringUtils.isEmpty(facilities)){
            List<Facility> facilityList = facilityMapper
                    .selectList(Wrappers.lambdaQuery(Facility.class)
                            .eq(Facility::getFacilityTypeId, typeId)
                            .eq(Facility::getStatus, StatusEnum.RUN.getCode())
                            .orderByAsc(Facility::getId));
            if(StringUtils.isEmpty(facilityList)){
                throw new FacilityNotFoundException(ErrorConst.FACILITY_NOT_FOUND);
            }
            facilities = facilityList
                            .stream()
                            .map(facility -> BeanUtil.copyProperties(facility, FacilityVO.class))
                            .toList();
            redisCache.set(RedisKeyBuild.createKey(RedisKeyManage.FACILITY_FOR_TYPE, typeId), facilities, DateUtils.DAY_SECONDS, TimeUnit.SECONDS);
        }
        return facilities;
    }

    @Override
    public FacilityVO getFacilityById(Long id) {
        FacilityVO facilityVO = redisCache.get(RedisKeyBuild.createKey(RedisKeyManage.FACILITY_DETAILS, id), FacilityVO.class);
        if(StringUtils.isNull(facilityVO)){
            Facility facility = facilityMapper
                    .selectOne(Wrappers.lambdaQuery(Facility.class)
                            .eq(Facility::getId, id));
            if(StringUtils.isNull(facility)){
                throw new FacilityNotFoundException(ErrorConst.FACILITY_NOT_FOUND);
            }
            facilityVO = BeanUtil.copyProperties(facility, FacilityVO.class);
            redisCache.set(RedisKeyBuild.createKey(RedisKeyManage.FACILITY_DETAILS, id), facilityVO, DateUtils.DAY_SECONDS, TimeUnit.SECONDS);
        }
        return facilityVO;
    }

    @Override
    public String getFacilityMarkers(Long typeId) {
        List<Facility> facilities = facilityMapper
                .selectList(Wrappers.lambdaQuery(Facility.class)
                        .eq(Facility::getFacilityTypeId, typeId)
                        .eq(Facility::getStatus, StatusEnum.RUN.getCode()));
        if(StringUtils.isEmpty(facilities)){
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for(int i = 0;i < facilities.size();i ++){
            Facility facility = facilities.get(i);
            if(i > 0){
                sb.append("|");
            }
            sb.append("mid,0xFF0000,")
                    .append(facility.getName())
                    .append(":")
                    .append(facility.getLongitude())
                    .append(",")
                    .append(facility.getLatitude());
        }
        return sb.toString();
    }

    @Override
    public String getNavigationUrl(FacilityMapRequestDTO request) {
        if(StringUtils.isNull(request.getFacilityId())){
            throw new DataNotExistException(ErrorConst.FACILITY_ID_NOT_NULL);
        }
        Facility facility = facilityMapper
                .selectOne(Wrappers.lambdaQuery(Facility.class)
                    .eq(Facility::getId, request.getFacilityId())
                    .eq(Facility::getStatus, StatusEnum.RUN.getCode()));
        if(StringUtils.isNull((facility))){
            throw new FacilityNotFoundException(ErrorConst.FACILITY_NOT_FOUND);
        }
        // 默认使用驾车导航
        int navType = request.getNavType() != null ? request.getNavType() : MapConstant.NavigationType.DRIVING;

        try {
            // 生成导航URL
            return gaodeMapUtil.generateNavigationUrl(
                    facility.getLongitude(),
                    facility.getLatitude(),
                    facility.getName(),
                    navType);

        } catch (Exception e) {
            log.error(ErrorConst.NAVIGATION_FAILED, e);
            throw new MapServerException(ErrorConst.NAVIGATION_FAILED);
        }
    }

    // TODO 目前直接返回JSONObject，待封装
    @Override
    public JSONObject getRoutePlanning(FacilityMapRequestDTO request) {
        if(StringUtils.isEmpty(request.getUserLongitude()) || StringUtils.isEmpty(request.getUserLatitude())){
            throw new DataNotExistException(ErrorConst.DATA_NOT_FOUND);
        }
        Facility facility = facilityMapper
                .selectOne(Wrappers.lambdaQuery(Facility.class)
                        .eq(Facility::getId, request.getFacilityId())
                        .eq(Facility::getStatus, StatusEnum.RUN.getCode()));
        if(StringUtils.isNull((facility))){
            throw new FacilityNotFoundException(ErrorConst.FACILITY_NOT_FOUND);
        }

        // 默认使用驾车导航
        int navType = request.getNavType() != null ? request.getNavType() : MapConstant.NavigationType.DRIVING;

        try {
            // 调用高德地图路径规划API
            return gaodeMapUtil.routePlanning(
                    request.getUserLongitude(),
                    request.getUserLatitude(),
                    facility.getLongitude(),
                    facility.getLatitude(),
                    navType);

        } catch (Exception e) {
            log.error(ErrorConst.ROUTE_PLAN_ERROR, e);
            throw new MapServerException(ErrorConst.NAVIGATION_FAILED);
        }

    }

    @Override
    public CalculateDistanceMapVO calculateDistance(FacilityMapRequestDTO request) {
        if(StringUtils.isEmpty(request.getUserLongitude()) || StringUtils.isEmpty(request.getUserLatitude())){
            throw new DataNotExistException(ErrorConst.DATA_NOT_FOUND);
        }
        Facility facility = facilityMapper
                .selectOne(Wrappers.lambdaQuery(Facility.class)
                        .eq(Facility::getId, request.getFacilityId())
                        .eq(Facility::getStatus, StatusEnum.RUN.getCode()));
        if(StringUtils.isNull((facility))){
            throw new FacilityNotFoundException(ErrorConst.FACILITY_NOT_FOUND);
        }

        // 默认使用直线距离
        int distanceType = request.getDistanceType() != null ? request.getDistanceType() : MapConstant.DistanceType.STRAIGHT_LINE;

        try {
            // 计算距离

            JSONObject jsonObject = gaodeMapUtil.calculateDistance(
                    request.getUserLongitude(),
                    request.getUserLatitude(),
                    facility.getLongitude(),
                    facility.getLatitude(),
                    distanceType);

            if(jsonObject == null || jsonObject.isEmpty()){
                log.error(ErrorConst.CALCULATE_DISTANCE_ERROR);
                throw new MapServerException(ErrorConst.CALCULATE_DISTANCE_ERROR);
            }

            JSONObject result = jsonObject.getJSONObject("results").getJSONObject("result");
            CalculateDistanceMapVO vo = new CalculateDistanceMapVO();

            vo.setDistance(result.getStr("distance"));
            vo.setFormattedDistance(formatDistance(Integer.valueOf(result.getStr("distance"))));
            vo.setDuration(result.getStr("duration"));
            vo.setFormattedDuration(formatDuration(Integer.valueOf(result.getStr("duration"))));

            return vo;


        } catch (Exception e) {
            log.error(ErrorConst.CALCULATE_DISTANCE_ERROR, e);
            throw new MapServerException(ErrorConst.CALCULATE_DISTANCE_ERROR);
        }
    }

    @Override
    public List<NearByFacilityVO> findNearbyFacilities(String longitude, String latitude, Integer radius, String type, Integer page, Integer limit) {
        if(StringUtils.isEmpty(longitude) || StringUtils.isEmpty(latitude)){
            throw new DataNotExistException(ErrorConst.DATA_NOT_FOUND);
        }

        // 设置默认值
        // 默认5公里
        int searchRadius = radius != null ? radius : 5000;
        int pageNum = page != null ? page : 1;
        int pageSize = limit != null ? limit : 10;

        JSONObject result = gaodeMapUtil.searchAround(longitude, latitude, "", searchRadius, type, pageNum, pageSize);

        JSONArray pois = result.getJSONArray("pois");
        if (pois == null || pois.isEmpty()) {
            return Collections.emptyList();
        }

        // 转换为 FacilityMapVO
        List<NearByFacilityVO> facilities = new ArrayList<>();
        for (int i = 0; i < pois.size(); i++) {
            JSONObject poi = pois.getJSONObject(i);
            NearByFacilityVO vo = new NearByFacilityVO();
            vo.setName(poi.getStr("name"));
            vo.setAddress(poi.getStr("address"));
            vo.setContactPhone(poi.getStr("tel"));
            vo.setDistance(poi.getStr("distance") != null ? poi.getStr("distance") : null);
            vo.setPoiId(poi.getStr("typecode"));
            vo.setFormattedDistance(formatDistance(poi.getStr("distance") != null ? Integer.valueOf(poi.getStr("distance")) : null));
            // 经纬度处理（高德返回 "lng,lat"）
            String location = poi.getStr("location");
            if (StringUtils.isNotEmpty(location)) {
                String[] coords = location.split(",");
                if (coords.length == 2) {
                    vo.setLongitude(coords[0]);
                    vo.setLatitude(coords[1]);
                }
            }

            facilities.add(vo);
        }

        return facilities;

    }

    @Override
    public PageVo<FacilityVO> getFacilityPage(FacilityQueryDTO queryDTO) {
        IPage<Facility> page = PageUtil.getPageParams(queryDTO);
        LambdaQueryWrapper<Facility> wrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotNull(queryDTO)){
            wrapper.like(StringUtils.isNotEmpty(queryDTO.getName()), Facility::getName, queryDTO.getName())
                    .like(StringUtils.isNotEmpty(queryDTO.getAddressKeyword()), Facility::getAddress, queryDTO.getAddressKeyword())
                    .eq(StringUtils.isNotNull(queryDTO.getStatus()), Facility::getStatus, queryDTO.getStatus());
            if(StringUtils.isNotNull(queryDTO.getCreateTimeStart())
                    && StringUtils.isNotNull(queryDTO.getCreateTimeEnd())
                    && queryDTO.getCreateTimeStart().before(queryDTO.getCreateTimeEnd())){
                wrapper.between(Facility::getCreateTime, queryDTO.getCreateTimeStart(), queryDTO.getCreateTimeEnd());
            }
        }
        IPage<Facility> facilityPage = facilityMapper.selectPage(page, wrapper);
        return PageUtil.convertPage(facilityPage, facility -> BeanUtil.copyProperties(facility, FacilityVO.class));

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addFacility(FacilitySaveDTO saveDTO) {
        if(StringUtils.isNull(saveDTO.getFacilityTypeId())
            || StringUtils.isEmpty(saveDTO.getLatitude())
            || StringUtils.isEmpty(saveDTO.getLongitude())){
            throw new DataNotExistException(ErrorConst.DATA_NOT_FOUND);
        }
        if(!facilityTypeMapper.exists(
                Wrappers.lambdaQuery(FacilityType.class)
                        .eq(FacilityType::getId, saveDTO.getFacilityTypeId())
                        .eq(FacilityType::getStatus, StatusEnum.RUN.getCode()))){
            throw new FacilityTypeNotFoundException(ErrorConst.FACILITY_TYPE_NOT_FOUND);
        }

        if(facilityMapper.exists(
                Wrappers.lambdaQuery(Facility.class)
                        .eq(Facility::getName, saveDTO.getName()))){
            throw new FacilityHasExistException(ErrorConst.FACILITY_HAS_EXIST);
        }

        Facility facility = BeanUtil.copyProperties(saveDTO, Facility.class);
        facilityMapper.insert(facility);
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.FACILITY_FOR_TYPE, saveDTO.getFacilityTypeId()));
        return facility.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateFacility(FacilitySaveDTO saveDTO) {
        if(StringUtils.isNull(saveDTO)
                || StringUtils.isNull(saveDTO.getFacilityTypeId())
                || StringUtils.isEmpty(saveDTO.getLatitude())
                || StringUtils.isEmpty(saveDTO.getLongitude())){
            throw new DataNotExistException(ErrorConst.DATA_NOT_FOUND);
        }
        if(StringUtils.isNull(saveDTO.getId())){
            throw new DataNotExistException(ErrorConst.FACILITY_ID_NOT_NULL);
        }

        if(!facilityTypeMapper.exists(
                Wrappers.lambdaQuery(FacilityType.class)
                        .eq(FacilityType::getId, saveDTO.getFacilityTypeId())
                        .eq(FacilityType::getStatus, StatusEnum.RUN.getCode()))){
            throw new FacilityTypeNotFoundException(ErrorConst.FACILITY_TYPE_NOT_FOUND);
        }

        if(!facilityMapper.exists(
                Wrappers.lambdaQuery(Facility.class)
                        .eq(Facility::getId, saveDTO.getId()))){
            throw new FacilityNotFoundException(ErrorConst.FACILITY_NOT_FOUND);
        }

        if(facilityMapper.exists(
                Wrappers.lambdaQuery(Facility.class)
                        .eq(Facility::getFacilityTypeId, saveDTO.getFacilityTypeId())
                        .eq(Facility::getName, saveDTO.getName()))){
            throw new FacilityHasExistException(ErrorConst.FACILITY_HAS_EXIST);

        }

        Facility facility = new Facility();
        BeanUtil.copyProperties(saveDTO, facility, CopyOptions.create().ignoreNullValue());
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.FACILITY_DETAILS, saveDTO.getId()));
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.FACILITY_FOR_TYPE, saveDTO.getFacilityTypeId()));
        return updateById(facility);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(List<Long> ids) {
        List<RedisKeyBuild> keys = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            List<Long> typeIds = facilityMapper.selectList(
                            Wrappers.<Facility>lambdaQuery()
                                    .in(Facility::getId, ids)
                                    .select(Facility::getFacilityTypeId)
                    ).stream()
                    .map(Facility::getFacilityTypeId)
                    .distinct()
                    .toList();
            ids.forEach(id ->
                    keys.add(RedisKeyBuild.createKey(RedisKeyManage.FACILITY_DETAILS, id))
            );
            typeIds.forEach(typeId ->
                    keys.add(RedisKeyBuild.createKey(RedisKeyManage.FACILITY_FOR_TYPE, typeId))
            );
        }
        redisCache.del(keys);
        return removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateFacilityStatus(Long id, Integer status) {
        Facility facility = facilityMapper.selectOne(Wrappers.lambdaQuery(Facility.class).eq(Facility::getId, id));
        if(StringUtils.isNull(facility)){
            throw new FacilityNotFoundException(ErrorConst.FACILITY_NOT_FOUND);
        }
        facility.setStatus(status);
        if(!updateById(facility)){
            return false;
        }
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.FACILITY_DETAILS, id));
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.FACILITY_FOR_TYPE, facility.getFacilityTypeId()));
        return true;
    }


    /**
     * 格式化距离
     *
     * @param distance 距离（米）
     * @return 格式化后的距离
     */
    private String formatDistance(Integer distance) {
        if (distance == null) {
            return "未知距离";
        }

        if (distance < 1000) {
            return distance + "米";
        } else {
            float km = distance / 1000.0f;
            return String.format("%.1f公里", km);
        }
    }

    /**
     * 格式化时间
     *
     * @param duration 时间（秒）
     * @return 格式化后的时间
     */
    private String formatDuration(Integer duration) {
        if (duration == null) {
            return "未知时间";
        }

        if (duration < 60) {
            return duration + "秒";
        } else if (duration < 3600) {
            int minutes = duration / 60;
            return minutes + "分钟";
        } else {
            int hours = duration / 3600;
            int minutes = (duration % 3600) / 60;
            return hours + "小时" + (minutes > 0 ? minutes + "分钟" : "");
        }
    }
}
