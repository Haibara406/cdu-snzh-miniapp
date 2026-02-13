package com.snzh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snzh.domain.dto.CheckinHistoryQueryDTO;
import com.snzh.domain.dto.CheckinRequestDTO;
import com.snzh.domain.entity.ScenicCheckin;
import com.snzh.domain.vo.BadgeCollectionVO;
import com.snzh.domain.vo.CheckinHistoryVO;
import com.snzh.domain.vo.CheckinResponseVO;
import com.snzh.domain.vo.PageVo;
import com.snzh.mapper.ScenicCheckinMapper;
import com.snzh.service.IScenicCheckinService;
import com.snzh.utils.DifyApiClient;
import com.snzh.utils.PageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * @author haibara
 * @description 景区签到服务实现
 * @since 2025/02/13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScenicCheckinServiceImpl extends ServiceImpl<ScenicCheckinMapper, ScenicCheckin> implements IScenicCheckinService {

    private final DifyApiClient difyApiClient;
    private final ScenicCheckinMapper scenicCheckinMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckinResponseVO checkin(Long userId, CheckinRequestDTO checkinRequestDTO) {
        log.info("用户 {} 发起签到，经度: {}, 纬度: {}", userId, checkinRequestDTO.getLongitude(), checkinRequestDTO.getLatitude());

        try {
            // 调用 Dify API 进行地理围栏判断
            DifyApiClient.DifyApiResponse difyResponse = difyApiClient.checkGeofence(
                    checkinRequestDTO.getLongitude(),
                    checkinRequestDTO.getLatitude(),
                    String.valueOf(userId)
            );

            // 判断是否签到成功
            boolean isSignSuccess = "True".equalsIgnoreCase(difyResponse.getIsSign());

            if (isSignSuccess) {
                // 检查今天是否已经在该景点签到过
                LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

                LambdaQueryWrapper<ScenicCheckin> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ScenicCheckin::getUserId, userId)
                        .eq(ScenicCheckin::getSpotId, difyResponse.getSpotId())
                        .between(ScenicCheckin::getCheckinTime, startOfDay, endOfDay)
                        .eq(ScenicCheckin::getStatus, 1);

                long count = this.count(queryWrapper);

                if (count > 0) {
                    log.info("用户 {} 今天已在景点 {} 签到过", userId, difyResponse.getSpotName());
                    return CheckinResponseVO.builder()
                            .success(false)
                            .spotName(difyResponse.getSpotName())
                            .spotId(difyResponse.getSpotId())
                            .badgeImageUrl(difyResponse.getImageUrl())
                            .distance(difyResponse.getDistance())
                            .message("您今天已在此景点签到过")
                            .build();
                }

                // 创建签到记录
                ScenicCheckin checkin = ScenicCheckin.builder()
                        .userId(userId)
                        .spotId(difyResponse.getSpotId())
                        .spotName(difyResponse.getSpotName())
                        .checkinTime(LocalDateTime.now())
                        .longitude(checkinRequestDTO.getLongitude())
                        .latitude(checkinRequestDTO.getLatitude())
                        .distance(difyResponse.getDistance())
                        .badgeImageUrl(difyResponse.getImageUrl())
                        .fenceRadius(difyResponse.getFenceRadius())
                        .status(1)
                        .build();

                this.save(checkin);
                log.info("用户 {} 签到成功，景点: {}", userId, difyResponse.getSpotName());

                return CheckinResponseVO.builder()
                        .success(true)
                        .spotName(difyResponse.getSpotName())
                        .spotId(difyResponse.getSpotId())
                        .badgeImageUrl(difyResponse.getImageUrl())
                        .distance(difyResponse.getDistance())
                        .message("签到成功！获得景点勋章")
                        .build();
            } else {
                // 签到失败，不在围栏内
                log.info("用户 {} 签到失败，不在景点围栏内，距离: {} 米", userId, difyResponse.getDistance());
                return CheckinResponseVO.builder()
                        .success(false)
                        .spotName(difyResponse.getSpotName())
                        .spotId(difyResponse.getSpotId())
                        .distance(difyResponse.getDistance())
                        .message(String.format("您距离 %s 还有 %s 米", difyResponse.getSpotName(), difyResponse.getDistance()))
                        .build();
            }

        } catch (Exception e) {
            log.error("签到失败", e);
            throw new RuntimeException("签到失败: " + e.getMessage(), e);
        }
    }

    @Override
    public PageVo<CheckinHistoryVO> getCheckinHistory(Long userId, CheckinHistoryQueryDTO queryDTO) {
        log.info("查询用户 {} 的签到历史", userId);
        
        IPage<CheckinHistoryVO> page = PageUtil.getPageParams(queryDTO);
        IPage<CheckinHistoryVO> resultPage = scenicCheckinMapper.selectCheckinHistoryPage(page, userId, queryDTO.getSpotId());
        
        return PageUtil.convertPage(resultPage, vo -> vo);
    }

    @Override
    public List<BadgeCollectionVO> getBadgeCollection(Long userId) {
        log.info("查询用户 {} 的勋章收藏", userId);
        return scenicCheckinMapper.selectBadgeCollection(userId);
    }

    @Override
    public PageVo<CheckinHistoryVO> getCheckinHistoryForAdmin(CheckinHistoryQueryDTO queryDTO) {
        log.info("管理端查询签到历史，查询条件: {}", queryDTO);
        
        IPage<CheckinHistoryVO> page = PageUtil.getPageParams(queryDTO);
        IPage<CheckinHistoryVO> resultPage = scenicCheckinMapper.selectCheckinHistoryPage(page, queryDTO.getUserId(), queryDTO.getSpotId());
        
        return PageUtil.convertPage(resultPage, vo -> vo);
    }
}
