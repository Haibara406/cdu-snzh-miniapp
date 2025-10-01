package com.snzh.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.snzh.constants.ErrorConst;
import com.snzh.domain.dto.TicketTypeQueryDTO;
import com.snzh.domain.dto.TicketTypeSaveDTO;
import com.snzh.domain.entity.TicketType;
import com.snzh.domain.vo.PageVo;
import com.snzh.domain.vo.TicketTypeVO;
import com.snzh.enums.StatusEnum;
import com.snzh.exceptions.DataNotExistException;
import com.snzh.exceptions.TicketTypeHasExistException;
import com.snzh.exceptions.TicketTypeNotFoundException;
import com.snzh.mapper.TicketTypeMapper;
import com.snzh.service.ITicketTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snzh.utils.PageUtil;
import com.snzh.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 票种表 服务实现类
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl extends ServiceImpl<TicketTypeMapper, TicketType> implements ITicketTypeService {

    private final TicketTypeMapper ticketTypeMapper;

    @Override
    public TicketTypeVO getTicketTypeDetail(Long id) {
        TicketType ticketType = ticketTypeMapper.selectOne(Wrappers.lambdaQuery(TicketType.class).eq(TicketType::getId, id));
        if(StringUtils.isNull(ticketType)){
            throw new TicketTypeNotFoundException(ErrorConst.TICKET_TYPE_NOT_EXIST);
        }
        return BeanUtil.copyProperties(ticketType, TicketTypeVO.class);
    }

    @Override
    public List<TicketTypeVO> listTicketTypes() {
        List<TicketType> ticketTypes = ticketTypeMapper.selectList(Wrappers.lambdaQuery(TicketType.class).eq(TicketType::getStatus, StatusEnum.RUN.getCode()));
        if(StringUtils.isEmpty(ticketTypes)){
            return null;
        }
        return ticketTypes
                .stream()
                .map(ticketType -> BeanUtil.copyProperties(ticketType, TicketTypeVO.class))
                .toList();
    }

    @Override
    public PageVo<TicketTypeVO> getTicketTypePage(TicketTypeQueryDTO queryDTO) {
        IPage<TicketType> page = PageUtil.getPageParams(queryDTO);
        LambdaQueryWrapper<TicketType> wrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotNull(queryDTO)){
            wrapper.like(StringUtils.isNotEmpty(queryDTO.getName()), TicketType::getName, queryDTO.getName())
                    .eq(StringUtils.isNotNull(queryDTO.getStatus()), TicketType::getStatus, queryDTO.getStatus())
                    .eq(StringUtils.isNotNull(queryDTO.getValidDays()), TicketType::getValidDays, queryDTO.getValidDays());
            if(StringUtils.isNotNull(queryDTO.getCreateTimeStart())
            && StringUtils.isNotNull(queryDTO.getCreateTimeEnd())
            && queryDTO.getCreateTimeStart().before(queryDTO.getCreateTimeEnd())){
                wrapper.between(TicketType::getCreateTime, queryDTO.getCreateTimeStart(), queryDTO.getCreateTimeEnd());
            }
        }
        IPage<TicketType> ticketTypeIPage = ticketTypeMapper.selectPage(page, wrapper);
        return PageUtil.convertPage(ticketTypeIPage, ticketType -> BeanUtil.copyProperties(ticketType, TicketTypeVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addTicketType(TicketTypeSaveDTO saveDTO) {
        if(StringUtils.isNull(saveDTO) || StringUtils.isEmpty(saveDTO.getName()) || StringUtils.isNull(saveDTO.getValidDays())){
            throw new DataNotExistException(ErrorConst.DATA_NOT_FOUND);
        }
        if(ticketTypeMapper.exists(Wrappers.lambdaQuery(TicketType.class).eq(TicketType::getName, saveDTO.getName()))){
            throw new TicketTypeHasExistException(ErrorConst.TICKET_TYPE_HAS_EXIST);
        }
        TicketType ticketType = BeanUtil.copyProperties(saveDTO, TicketType.class);
        ticketTypeMapper.insert(ticketType);
        return ticketType.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTicketType(TicketTypeSaveDTO saveDTO) {
        if(StringUtils.isNull(saveDTO.getId())){
            throw new DataNotExistException(ErrorConst.TICKET_TYPE_ID_NOT_NULL);
        }
        if(StringUtils.isEmpty(saveDTO.getName())){
            throw new DataNotExistException(ErrorConst.TICKET_TYPE_NAME_NOT_NULL);
        }
        TicketType ticketType = ticketTypeMapper.selectOne(Wrappers.lambdaQuery(TicketType.class).eq(TicketType::getId, saveDTO.getId()));
        if(StringUtils.isNull(ticketType)){
            throw new TicketTypeNotFoundException(ErrorConst.TICKET_TYPE_NOT_EXIST);
        }
        if(!ticketTypeMapper.exists(Wrappers.lambdaQuery(TicketType.class).eq(TicketType::getName, saveDTO.getName()))){
            throw new TicketTypeHasExistException(ErrorConst.TICKET_TYPE_HAS_EXIST);
        }
        BeanUtil.copyProperties(saveDTO, ticketType, CopyOptions.create().ignoreNullValue());
        ticketTypeMapper.updateById(ticketType);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(List<Long> ids) {
        return removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTicketTypeStatus(Long id, Integer status) {
        if(!ticketTypeMapper.exists(Wrappers.lambdaQuery(TicketType.class).eq(TicketType::getId, id))){
            throw new TicketTypeNotFoundException(ErrorConst.TICKET_TYPE_NOT_EXIST);
        }
        TicketType ticketType = TicketType.builder()
                .id(id)
                .status(status)
                .build();
        return updateById(ticketType);
    }
}
