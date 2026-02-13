package com.snzh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.snzh.domain.dto.CheckinHistoryQueryDTO;
import com.snzh.domain.dto.CheckinRequestDTO;
import com.snzh.domain.entity.ScenicCheckin;
import com.snzh.domain.vo.BadgeCollectionVO;
import com.snzh.domain.vo.CheckinHistoryVO;
import com.snzh.domain.vo.CheckinResponseVO;
import com.snzh.domain.vo.PageVo;

import java.util.List;

/**
 * @author haibara
 * @description 景区签到服务接口
 * @since 2025/02/13
 */
public interface IScenicCheckinService extends IService<ScenicCheckin> {

    /**
     * 用户签到
     *
     * @param userId           用户ID
     * @param checkinRequestDTO 签到请求
     * @return 签到响应
     */
    CheckinResponseVO checkin(Long userId, CheckinRequestDTO checkinRequestDTO);

    /**
     * 查询签到历史
     *
     * @param userId    用户ID
     * @param queryDTO  查询条件
     * @return 签到历史分页数据
     */
    PageVo<CheckinHistoryVO> getCheckinHistory(Long userId, CheckinHistoryQueryDTO queryDTO);

    /**
     * 查询勋章收藏
     *
     * @param userId 用户ID
     * @return 勋章收藏列表
     */
    List<BadgeCollectionVO> getBadgeCollection(Long userId);

    /**
     * 管理端：分页查询签到历史
     *
     * @param queryDTO 查询条件
     * @return 签到历史分页数据
     */
    PageVo<CheckinHistoryVO> getCheckinHistoryForAdmin(CheckinHistoryQueryDTO queryDTO);
}
