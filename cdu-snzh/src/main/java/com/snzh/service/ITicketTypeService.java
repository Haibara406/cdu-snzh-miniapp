package com.snzh.service;

import com.snzh.domain.dto.TicketTypeQueryDTO;
import com.snzh.domain.dto.TicketTypeSaveDTO;
import com.snzh.domain.entity.TicketType;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snzh.domain.vo.PageVo;
import com.snzh.domain.vo.TicketTypeVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * <p>
 * 票种表 服务类
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
public interface ITicketTypeService extends IService<TicketType> {

    TicketTypeVO getTicketTypeDetail(Long id);

    List<TicketTypeVO> listTicketTypes();

    PageVo<TicketTypeVO> getTicketTypePage(@Valid TicketTypeQueryDTO queryDTO);

    Long addTicketType(@Valid TicketTypeSaveDTO saveDTO);

    Boolean updateTicketType(@Valid TicketTypeSaveDTO saveDTO);

    boolean deleteByIds(List<Long> ids);

    Boolean updateTicketTypeStatus(Long id, Integer status);
}
