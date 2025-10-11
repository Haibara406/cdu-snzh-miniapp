package com.snzh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.snzh.domain.entity.Order;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author haibara
 * @description 订单Mapper接口
 * @since 2025/10/2
 */
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 按状态统计用户订单数量
     * @param userId 用户ID
     * @return List<Map<statusCode, count>>
     */
    @Select("SELECT order_status as statusCode, COUNT(*) as count FROM orders WHERE user_id = #{userId} GROUP BY order_status")
    List<Map<String, Object>> countByStatusForUser(@Param("userId") Long userId);

    /**
     * 统计所有订单的状态分布
     * @return List<Map<statusCode, count>>
     */
    @Select("SELECT order_status as statusCode, COUNT(*) as count FROM orders GROUP BY order_status")
    List<Map<String, Object>> countByStatus();

    /**
     * 统计所有订单的类型分布
     * @return List<Map<typeCode, count>>
     */
    @Select("SELECT order_type as typeCode, COUNT(*) as count FROM orders GROUP BY order_type")
    List<Map<String, Object>> countByType();
}

