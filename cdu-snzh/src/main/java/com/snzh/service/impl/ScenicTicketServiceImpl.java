package com.snzh.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.snzh.constants.ErrorConst;
import com.snzh.domain.dto.TicketQueryDTO;
import com.snzh.domain.dto.TicketSaveDTO;
import com.snzh.domain.entity.ScenicSpot;
import com.snzh.domain.entity.ScenicTicket;
import com.snzh.domain.entity.TicketType;
import com.snzh.domain.vo.PageVo;
import com.snzh.domain.vo.ScenicTicketVO;
import com.snzh.enums.RedisKeyManage;
import com.snzh.enums.SpotTypeEnum;
import com.snzh.enums.StatusEnum;
import com.snzh.exceptions.*;
import com.snzh.mapper.ScenicSpotMapper;
import com.snzh.mapper.ScenicTicketMapper;
import com.snzh.mapper.TicketTypeMapper;
import com.snzh.redis.RedisCache;
import com.snzh.redis.RedisKeyBuild;
import com.snzh.service.IScenicTicketService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snzh.utils.DateUtils;
import com.snzh.utils.PageUtil;
import com.snzh.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 景点票种关联表 服务实现类
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScenicTicketServiceImpl extends ServiceImpl<ScenicTicketMapper, ScenicTicket> implements IScenicTicketService {

    private final ScenicTicketMapper scenicTicketMapper;
    private final RedisCache redisCache;
    private final ScenicSpotMapper scenicSpotMapper;
    private final TicketTypeMapper ticketTypeMapper;

    @Override
    public List<ScenicTicketVO> findTicketsByScenicId(Long scenicId) {
        List<ScenicTicketVO> scenicTickets = redisCache.getValueIsList(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_TICKET_FOR_SPOT, scenicId), ScenicTicketVO.class);
        if(StringUtils.isEmpty(scenicTickets)){
            List<ScenicTicket> scenicTicketList = scenicTicketMapper.selectList(
                    Wrappers.lambdaQuery(ScenicTicket.class)
                            .eq(ScenicTicket::getScenicSpotId, scenicId)
                            .eq(ScenicTicket::getStatus, StatusEnum.RUN.getCode()));
            if(StringUtils.isEmpty(scenicTicketList)){
                throw new ScenicTicketNotFoundException(ErrorConst.SCENIC_TICKET_NOT_FOUND);
            }
            scenicTickets = scenicTicketList
                            .stream()
                            .map(scenicTicket -> BeanUtil.copyProperties(scenicTicket, ScenicTicketVO.class))
                            .toList();
            redisCache.set(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_TICKET_FOR_SPOT, scenicId), scenicTickets, DateUtils.DAY_SECONDS, TimeUnit.SECONDS);
        }
        return scenicTickets;
    }

    @Override
    public ScenicTicketVO getScenicTicketDetail(Long id) {
        ScenicTicketVO scenicTicketVO = redisCache.get(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_TICKET_DETAIL, id), ScenicTicketVO.class);
        if(StringUtils.isNull(scenicTicketVO)){
            ScenicTicket scenicTicket = scenicTicketMapper.selectOne(Wrappers.lambdaQuery(ScenicTicket.class).eq(ScenicTicket::getId, id));
            if(StringUtils.isNull(scenicTicket)){
                throw new ScenicTicketNotFoundException(ErrorConst.SCENIC_TICKET_NOT_FOUND);
            }
            scenicTicketVO = BeanUtil.copyProperties(scenicTicket, ScenicTicketVO.class);
            redisCache.set(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_TICKET_DETAIL, id), scenicTicketVO, DateUtils.DAY_SECONDS, TimeUnit.SECONDS);
        }
        return scenicTicketVO;
    }

    @Override
    public PageVo<ScenicTicketVO> getTicketPage(TicketQueryDTO queryDTO) {
        IPage<ScenicTicket> page = PageUtil.getPageParams(queryDTO);
        LambdaQueryWrapper<ScenicTicket> wrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotNull(queryDTO)){
            wrapper.eq(StringUtils.isNotNull(queryDTO.getScenicSpotId()), ScenicTicket::getScenicSpotId, queryDTO.getScenicSpotId())
                    .eq(StringUtils.isNotNull(queryDTO.getStatus()), ScenicTicket::getStatus, queryDTO.getStatus());
            if(StringUtils.isNotNull(queryDTO.getCreateTimeStart())
            && StringUtils.isNotNull(queryDTO.getCreateTimeEnd())
            && queryDTO.getCreateTimeStart().before(queryDTO.getCreateTimeEnd())){
                wrapper.between(ScenicTicket::getCreateTime, queryDTO.getCreateTimeStart(), queryDTO.getCreateTimeEnd());
            }
        }
        wrapper.orderByAsc(ScenicTicket::getSoldCount);
        IPage<ScenicTicket> scenicTicketPage = scenicTicketMapper.selectPage(page, wrapper);
        return PageUtil.convertPage(scenicTicketPage, scenicTicket -> BeanUtil.copyProperties(scenicTicket, ScenicTicketVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addTicket(TicketSaveDTO saveDTO) {
        if(StringUtils.isNull(saveDTO)
                || StringUtils.isNull(saveDTO.getScenicSpotId())
                || StringUtils.isNull(saveDTO.getTicketTypeId())){
            throw new DataNotExistException(ErrorConst.DATA_NOT_FOUND);
        }

        if(!scenicSpotMapper.exists(
                Wrappers.lambdaQuery(ScenicSpot.class)
                        .eq(ScenicSpot::getId, saveDTO.getScenicSpotId())
                        .eq(ScenicSpot::getStatus, StatusEnum.RUN.getCode())
                        .eq(ScenicSpot::getSpotType, SpotTypeEnum.SCENIC_AREA.getCode()))){
            throw new ScenicSpotNotFoundException(ErrorConst.SCENIC_SPOT_NOT_FOUND);
        }

        if(!ticketTypeMapper.exists(
                Wrappers.lambdaQuery(TicketType.class)
                        .eq(TicketType::getId, saveDTO.getTicketTypeId())
                        .eq(TicketType::getStatus, StatusEnum.RUN.getCode()))){
            throw new TicketTypeNotFoundException(ErrorConst.TICKET_TYPE_NOT_EXIST);
        }

        if(scenicTicketMapper.exists(
                Wrappers.lambdaQuery(ScenicTicket.class)
                        .eq(ScenicTicket::getTicketTypeId, saveDTO.getTicketTypeId())
                        .eq(ScenicTicket::getScenicSpotId, saveDTO.getScenicSpotId()))){
            throw new ScenicTicketHasExistException(ErrorConst.SCENIC_TICKET_HAS_EXIST);
        }

        ScenicTicket scenicTicket = BeanUtil.copyProperties(saveDTO, ScenicTicket.class);
        scenicTicketMapper.insert(scenicTicket);
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_TICKET_FOR_SPOT, saveDTO.getScenicSpotId()));
        return scenicTicket.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTicket(TicketSaveDTO saveDTO) {
        if(StringUtils.isNull(saveDTO)
                || StringUtils.isNull(saveDTO.getScenicSpotId())
                || StringUtils.isNull(saveDTO.getTicketTypeId())){
            throw new DataNotExistException(ErrorConst.DATA_NOT_FOUND);
        }
        if(StringUtils.isNull(saveDTO.getId())){
            throw new DataNotExistException(ErrorConst.SCENIC_TICKET_ID_NOT_NULL);
        }

        if(!scenicSpotMapper.exists(
                Wrappers.lambdaQuery(ScenicSpot.class)
                        .eq(ScenicSpot::getId, saveDTO.getScenicSpotId())
                        .eq(ScenicSpot::getStatus, StatusEnum.RUN.getCode())
                        .eq(ScenicSpot::getSpotType, SpotTypeEnum.SCENIC_AREA.getCode()))){
            throw new ScenicSpotNotFoundException(ErrorConst.SCENIC_SPOT_NOT_FOUND);
        }

        if(!ticketTypeMapper.exists(
                Wrappers.lambdaQuery(TicketType.class)
                        .eq(TicketType::getId, saveDTO.getTicketTypeId())
                        .eq(TicketType::getStatus, StatusEnum.RUN.getCode()))){
            throw new TicketTypeNotFoundException(ErrorConst.TICKET_TYPE_NOT_EXIST);
        }

        if(!scenicTicketMapper.exists(
                Wrappers.lambdaQuery(ScenicTicket.class)
                        .eq(ScenicTicket::getId, saveDTO.getId()))){
            throw new ScenicTicketNotFoundException(ErrorConst.SCENIC_TICKET_NOT_FOUND);
        }

        if(scenicTicketMapper.exists(
                Wrappers.lambdaQuery(ScenicTicket.class)
                        .eq(ScenicTicket::getTicketTypeId, saveDTO.getTicketTypeId())
                        .eq(ScenicTicket::getScenicSpotId, saveDTO.getScenicSpotId())
                        .ne(ScenicTicket::getId, saveDTO.getId()))){
            throw new ScenicTicketHasExistException(ErrorConst.SCENIC_TICKET_HAS_EXIST);
        }

        ScenicTicket scenicTicket = new ScenicTicket();
        BeanUtil.copyProperties(saveDTO, scenicTicket, CopyOptions.create().ignoreNullValue());
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_TICKET_DETAIL, saveDTO.getId()));
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_TICKET_FOR_SPOT, saveDTO.getScenicSpotId()));
        return updateById(scenicTicket);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(List<Long> ids) {
        List<RedisKeyBuild> keys = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            List<Long> scenicIds = scenicTicketMapper.selectList(
                            Wrappers.lambdaQuery(ScenicTicket.class)
                                    .in(ScenicTicket::getId, ids)
                                    .select(ScenicTicket::getScenicSpotId))
                    .stream()
                    .map(ScenicTicket::getScenicSpotId)
                    .distinct()
                    .toList();
            ids.forEach(id ->
                    keys.add(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_TICKET_DETAIL, id)));
            scenicIds.forEach(id ->
                    keys.add(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_TICKET_FOR_SPOT, id)));
        }
        redisCache.del(keys);
        return removeBatchByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTicketStatus(Long id, Integer status) {
        ScenicTicket scenicTicket = scenicTicketMapper.selectOne(Wrappers.lambdaQuery(ScenicTicket.class).eq(ScenicTicket::getId, id));
        if(StringUtils.isNull(scenicTicket)){
            throw new ScenicTicketNotFoundException(ErrorConst.SCENIC_TICKET_NOT_FOUND);
        }
        scenicTicket.setStatus(status);
        if(!updateById(scenicTicket)){
            return false;
        }
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_TICKET_DETAIL, id));
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_TICKET_FOR_SPOT, scenicTicket.getScenicSpotId()));
        return true;
    }
}
