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
import com.snzh.domain.dto.ScenicQueryDTO;
import com.snzh.domain.dto.ScenicSpotMapRequestDTO;
import com.snzh.domain.dto.ScenicSpotSaveDTO;
import com.snzh.domain.entity.ScenicSpot;
import com.snzh.domain.vo.*;
import com.snzh.enums.RedisKeyManage;
import com.snzh.enums.StatusEnum;
import com.snzh.exceptions.*;
import com.snzh.mapper.ScenicSpotMapper;
import com.snzh.mapper.ScenicTicketMapper;
import com.snzh.mapper.TicketTypeMapper;
import com.snzh.redis.RedisCache;
import com.snzh.redis.RedisKeyBuild;
import com.snzh.service.IScenicSpotService;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 景点表 服务实现类
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScenicSpotServiceImpl extends ServiceImpl<ScenicSpotMapper, ScenicSpot> implements IScenicSpotService {

    private final ScenicSpotMapper scenicSpotMapper;
    private final ScenicTicketMapper scenicTicketMapper;
    private final TicketTypeMapper ticketTypeMapper;
    private final RedisCache redisCache;
    private final GaodeMapUtil gaodeMapUtil;

    @Override
    public ScenicSpotVO getScenicSpotDetail(Long id) {
        if(StringUtils.isNull(id)){
            throw new DataNotExistException(ErrorConst.SCENIC_SPOT_ID_NOT_NULL);
        }
        ScenicSpotVO scenicSpotVO = redisCache.get(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_SPOT_DETAIL, id), ScenicSpotVO.class);
        if(StringUtils.isNull(scenicSpotVO)){
            ScenicSpot scenicSpot = scenicSpotMapper.selectOne(Wrappers.lambdaQuery(ScenicSpot.class).eq(ScenicSpot::getId, id));
            if(StringUtils.isNull(scenicSpot)){
                throw new ScenicSpotNotFoundException(ErrorConst.SCENIC_SPOT_NOT_FOUND);
            }
            scenicSpotVO = BeanUtil.copyProperties(scenicSpot, ScenicSpotVO.class);
            redisCache.set(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_SPOT_DETAIL, id), scenicSpotVO, DateUtils.DAY_SECONDS, TimeUnit.SECONDS);
        }
        return scenicSpotVO;
    }

    @Override
    public List<ScenicSpotVO> getScenicSpotList() {
        List<ScenicSpotVO> scenicSpotVOList = redisCache.getValueIsList(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_SPOT_LIST), ScenicSpotVO.class);
        if(StringUtils.isEmpty(scenicSpotVOList)){
            List<ScenicSpot> scenicSpotList = scenicSpotMapper.selectList(Wrappers.lambdaQuery(ScenicSpot.class).eq(ScenicSpot::getStatus, StatusEnum.RUN.getCode()));
            if(StringUtils.isEmpty(scenicSpotList)){
                throw new ScenicSpotNotFoundException(ErrorConst.SCENIC_SPOT_NOT_FOUND);
            }
            scenicSpotVOList = scenicSpotList
                        .stream()
                        .map(scenicSpot -> BeanUtil.copyProperties(scenicSpot, ScenicSpotVO.class))
                        .toList();
            redisCache.set(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_SPOT_LIST), scenicSpotVOList, DateUtils.DAY_SECONDS, TimeUnit.SECONDS);
        }
        return scenicSpotVOList;
    }

    @Override
    public String getScenicMarkers() {
        List<ScenicSpot> scenicSpotList = scenicSpotMapper.selectList(
                Wrappers.lambdaQuery(ScenicSpot.class)
                        .eq(ScenicSpot::getStatus, StatusEnum.RUN.getCode()));
        if(StringUtils.isEmpty(scenicSpotList)){
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for(int i = 0;i < scenicSpotList.size() ;i ++){
            ScenicSpot scenicSpot = scenicSpotList.get(i);
            if(i > 0){
                sb.append("|");
            }
            sb.append("mid,0xFF0000,")
                    .append(scenicSpot.getName())
                    .append(":")
                    .append(scenicSpot.getLongitude())
                    .append(",")
                    .append(scenicSpot.getLatitude());
        }
        return sb.toString();
    }

    @Override
    public String getNavigationUrl(ScenicSpotMapRequestDTO dto) {
        if(StringUtils.isNull(dto)){
            throw new DataNotExistException(ErrorConst.DATA_NOT_FOUND);
        }
        ScenicSpot scenicSpot = scenicSpotMapper.selectOne(
                Wrappers.lambdaQuery(ScenicSpot.class)
                        .eq(ScenicSpot::getId, dto.getScenicId())
                        .eq(ScenicSpot::getStatus, StatusEnum.RUN.getCode()));
        if(StringUtils.isNull(scenicSpot)){
            throw new ScenicSpotNotFoundException(ErrorConst.SCENIC_SPOT_NOT_FOUND);
        }
        int navType = dto.getNavType() != null ? dto.getNavType() : MapConstant.NavigationType.DRIVING;

        try{
            return gaodeMapUtil.generateNavigationUrl(
                    scenicSpot.getLongitude(),
                    scenicSpot.getLatitude(),
                    scenicSpot.getName(),
                    navType);
        }catch (Exception e){
            log.error(ErrorConst.NAVIGATION_FAILED, e);
            throw new MapServerException(ErrorConst.NAVIGATION_FAILED);
        }

    }

    // TODO 目前直接返回JSONObject，待封装
    @Override
    public JSONObject getRoutePlanning(ScenicSpotMapRequestDTO dto) {
        if(StringUtils.isEmpty(dto.getUserLongitude()) || StringUtils.isEmpty(dto.getUserLatitude())){
            throw new DataNotExistException(ErrorConst.DATA_NOT_FOUND);
        }
        ScenicSpot scenicSpot = scenicSpotMapper
                .selectOne(Wrappers.lambdaQuery(ScenicSpot.class)
                        .eq(ScenicSpot::getId, dto.getScenicId())
                        .eq(ScenicSpot::getStatus, StatusEnum.RUN.getCode()));
        if(StringUtils.isNull((scenicSpot))){
            throw new ScenicSpotNotFoundException(ErrorConst.SCENIC_SPOT_NOT_FOUND);
        }

        // 默认使用驾车导航
        int navType = dto.getNavType() != null ? dto.getNavType() : MapConstant.NavigationType.DRIVING;

        try {
            // 调用高德地图路径规划API
            return gaodeMapUtil.routePlanning(
                    dto.getUserLongitude(),
                    dto.getUserLatitude(),
                    scenicSpot.getLongitude(),
                    scenicSpot.getLatitude(),
                    navType);

        } catch (Exception e) {
            log.error(ErrorConst.ROUTE_PLAN_ERROR, e);
            throw new MapServerException(ErrorConst.NAVIGATION_FAILED);
        }
    }

    @Override
    public CalculateDistanceMapVO calculateDistance(ScenicSpotMapRequestDTO dto) {
        if(StringUtils.isEmpty(dto.getUserLongitude()) || StringUtils.isEmpty(dto.getUserLatitude())){
            throw new DataNotExistException(ErrorConst.DATA_NOT_FOUND);
        }
        ScenicSpot scenicSpot = scenicSpotMapper
                .selectOne(Wrappers.lambdaQuery(ScenicSpot.class)
                        .eq(ScenicSpot::getId, dto.getScenicId())
                        .eq(ScenicSpot::getStatus, StatusEnum.RUN.getCode()));
        if(StringUtils.isNull((scenicSpot))){
            throw new FacilityNotFoundException(ErrorConst.FACILITY_NOT_FOUND);
        }

        // 默认使用直线距离
        int distanceType = dto.getDistanceType() != null ? dto.getDistanceType() : MapConstant.DistanceType.STRAIGHT_LINE;

        try {
            // 计算距离

            JSONObject jsonObject = gaodeMapUtil.calculateDistance(
                    dto.getUserLongitude(),
                    dto.getUserLatitude(),
                    scenicSpot.getLongitude(),
                    scenicSpot.getLatitude(),
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
    public List<NearByScenicSpotVO> findNearbySpots(String longitude, String latitude, Integer radius, String type, Integer page, Integer limit) {
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

        List<NearByScenicSpotVO> scenicSpot = new ArrayList<>();
        for (int i = 0; i < pois.size(); i++) {
            JSONObject poi = pois.getJSONObject(i);
            NearByScenicSpotVO vo = new NearByScenicSpotVO();
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

            scenicSpot.add(vo);
        }

        return scenicSpot;
    }

    @Override
    public String getScenicModel3dUrl(Long scenicId) {
        ScenicSpotVO scenicSpotVO = redisCache.get(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_SPOT_DETAIL, scenicId), ScenicSpotVO.class);
        if(StringUtils.isNull(scenicSpotVO) || StringUtils.isEmpty(scenicSpotVO.getModel3dUrl())){
            String model3dUrl = scenicSpotMapper.selectOne(
                            Wrappers.lambdaQuery(ScenicSpot.class)
                                    .select(ScenicSpot::getModel3dUrl)
                                    .eq(ScenicSpot::getId, scenicId)
                                    .eq(ScenicSpot::getStatus, StatusEnum.RUN.getCode()))
                    .getModel3dUrl();
            if(StringUtils.isEmpty(model3dUrl)){
                throw new ScenicSpotNotFoundException(ErrorConst.SCENIC_SPOT_NOT_FOUND);
            }
            scenicSpotVO.setModel3dUrl(model3dUrl);
            redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_SPOT_DETAIL, scenicId));
            redisCache.set(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_TICKET_DETAIL, scenicId), scenicSpotVO, DateUtils.DAY_SECONDS, TimeUnit.SECONDS);

            return model3dUrl;
        }
        return "";
    }

    @Override
    public PageVo<ScenicSpotVO> getScenicPage(ScenicQueryDTO dto) {
        IPage<ScenicSpot> page = PageUtil.getPageParams(dto);
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNull(dto)){
            wrapper.like(StringUtils.isNotEmpty(dto.getName()), ScenicSpot::getName, dto.getName())
                    .eq(StringUtils.isNotEmpty(dto.getAddressKeyword()), ScenicSpot::getAddress, dto.getAddressKeyword())
                    .eq(StringUtils.isNotNull(dto.getStatus()), ScenicSpot::getStatus, dto.getStatus());
            if(StringUtils.isNotNull(dto.getCreateTimeStart())
            && StringUtils.isNotNull(dto.getCreateTimeEnd())
            && dto.getCreateTimeStart().before(dto.getCreateTimeEnd())){
                wrapper.between(ScenicSpot::getCreateTime, dto.getCreateTimeStart(), dto.getCreateTimeEnd());
            }
        }
        IPage<ScenicSpot> scenicSpotPage = scenicSpotMapper.selectPage(page, wrapper);
        return PageUtil.convertPage(scenicSpotPage, scenicSpot -> BeanUtil.copyProperties(scenicSpot, ScenicSpotVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addScenicSpot(ScenicSpotSaveDTO dto) {
        if(StringUtils.isNull(dto)
                || StringUtils.isNull(dto.getName())
                || StringUtils.isNull(dto.getLatitude())
                || StringUtils.isNull(dto.getLongitude())){
            throw new DataNotExistException(ErrorConst.DATA_NOT_FOUND);
        }
        if(scenicSpotMapper.exists(Wrappers.lambdaQuery(ScenicSpot.class).eq(ScenicSpot::getId, dto.getId()))){
            throw new ScenicSpotHasExistException(ErrorConst.SCENIC_SPOT_HAS_EXIST);
        }
        ScenicSpot scenicSpot = BeanUtil.copyProperties(dto, ScenicSpot.class);
        scenicSpotMapper.insert(scenicSpot);
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_SPOT_LIST));
        return scenicSpot.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateScenicSpot(ScenicSpotSaveDTO dto) {
        if(StringUtils.isNull(dto)
                || StringUtils.isEmpty(dto.getName())
                || StringUtils.isNull(dto.getLongitude())
                || StringUtils.isNull(dto.getLatitude())){
            throw new DataNotExistException(ErrorConst.DATA_NOT_FOUND);
        }
        if(StringUtils.isNull(dto.getId())){
            throw new ScenicSpotNotFoundException(ErrorConst.SCENIC_SPOT_ID_NOT_NULL);
        }
        if(scenicSpotMapper.exists(
                Wrappers.lambdaQuery(ScenicSpot.class)
                        .eq(ScenicSpot::getLatitude, dto.getLatitude())
                        .eq(ScenicSpot::getLongitude, dto.getLongitude())
                        .eq(ScenicSpot::getName, dto.getName()))){
            throw new ScenicSpotHasExistException(ErrorConst.SCENIC_SPOT_HAS_EXIST);
        }
        if(!scenicSpotMapper.exists(
                Wrappers.lambdaQuery(ScenicSpot.class)
                        .eq(ScenicSpot::getId, dto.getId()))){
            throw new ScenicSpotNotFoundException(ErrorConst.SCENIC_SPOT_NOT_FOUND);
        }
        ScenicSpot scenicSpot = new ScenicSpot();
        BeanUtil.copyProperties(dto, scenicSpot, CopyOptions.create().ignoreNullValue());
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_SPOT_DETAIL, dto.getId()));
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_SPOT_LIST));
        return updateById(scenicSpot);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(List<Long> ids) {
        List<RedisKeyBuild> keys = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            ids.forEach(id ->
                    keys.add(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_SPOT_DETAIL, id))
            );
        }
        redisCache.del(keys);
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_SPOT_LIST));
        return removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateScenicSpotStatus(Long id, Integer status) {
        ScenicSpot scenicSpot = scenicSpotMapper.selectOne(Wrappers.lambdaQuery(ScenicSpot.class).eq(ScenicSpot::getId, id));
        if(StringUtils.isNull(scenicSpot)){
            throw new ScenicSpotNotFoundException(ErrorConst.SCENIC_SPOT_NOT_FOUND);
        }
        scenicSpot.setStatus(status);
        if(!updateById(scenicSpot)){
            return false;
        }
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_SPOT_DETAIL, id));
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_SPOT_LIST));
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
