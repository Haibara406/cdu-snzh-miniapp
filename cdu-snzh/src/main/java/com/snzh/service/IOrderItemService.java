package com.snzh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.snzh.domain.entity.OrderItem;
import com.snzh.domain.vo.OrderItemVO;

import java.util.List;

/**
 * @author haibara
 * @description 订单明细服务接口
 * @since 2025/10/2
 */
public interface IOrderItemService extends IService<OrderItem> {

    /**
     * 根据订单ID查询订单明细
     * @param orderId 订单ID
     * @return 订单明细VO列表
     */
    List<OrderItemVO> getByOrderId(Long orderId);

    /**
     * 批量创建订单明细
     * @param orderItems 订单明细列表
     * @return 是否成功
     */
    Boolean batchCreate(List<OrderItem> orderItems);
}

