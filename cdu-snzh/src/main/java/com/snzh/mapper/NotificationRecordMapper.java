package com.snzh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.snzh.domain.entity.NotificationRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author haibara
 * @description 消息通知记录Mapper
 * @since 2025/10/2
 */
@Mapper
public interface NotificationRecordMapper extends BaseMapper<NotificationRecord> {
}

