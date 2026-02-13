package com.snzh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snzh.domain.entity.ScenicCheckin;
import com.snzh.domain.vo.BadgeCollectionVO;
import com.snzh.domain.vo.CheckinHistoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author haibara
 * @description 景区签到Mapper
 * @since 2025/02/13
 */
@Mapper
public interface ScenicCheckinMapper extends BaseMapper<ScenicCheckin> {

    /**
     * 查询用户的勋章收藏（按景点分组）
     */
    List<BadgeCollectionVO> selectBadgeCollection(@Param("userId") Long userId);

    /**
     * 分页查询签到历史（带用户信息）
     */
    IPage<CheckinHistoryVO> selectCheckinHistoryPage(IPage<CheckinHistoryVO> page,
                                                      @Param("userId") Long userId,
                                                      @Param("spotId") String spotId);
}
