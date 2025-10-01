package com.snzh.service;

import com.snzh.domain.dto.TicketQueryDTO;
import com.snzh.domain.dto.TicketSaveDTO;
import com.snzh.domain.entity.ScenicTicket;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snzh.domain.vo.PageVo;
import com.snzh.domain.vo.ScenicTicketVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * <p>
 * 景点票种关联表 服务类
 * </p>
 *
 * @author haibara
 * @since 2025-09-20
 */
public interface IScenicTicketService extends IService<ScenicTicket> {

    List<ScenicTicketVO> findTicketsByScenicId(Long scenicId);

    ScenicTicketVO getScenicTicketDetail(Long id);

    PageVo<ScenicTicketVO> getTicketPage(@Valid TicketQueryDTO queryDTO);

    Long addTicket(@Valid TicketSaveDTO saveDTO);

    Boolean updateTicket(@Valid TicketSaveDTO saveDTO);

    boolean deleteByIds(List<Long> ids);

    Boolean updateTicketStatus(Long id, Integer status);
}
