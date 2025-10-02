package com.snzh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.snzh.domain.dto.OrderCancelDTO;
import com.snzh.domain.dto.OrderCreateDTO;
import com.snzh.domain.dto.OrderQueryDTO;
import com.snzh.domain.entity.Order;
import com.snzh.domain.vo.OrderListVO;
import com.snzh.domain.vo.OrderVO;
import com.snzh.domain.vo.PageVo;
import jakarta.validation.Valid;

import java.util.Map;

/**
 * @author haibara
 * @description 订单服务接口
 * @since 2025/10/2
 */
public interface IOrderService extends IService<Order> {

    /**
     * 创建订单
     * @param dto 创建订单请求参数
     * @return 订单号
     */
    String createOrder(@Valid OrderCreateDTO dto);

    /**
     * 支付订单（模拟）
     * @param orderNo 订单号
     * @return 是否成功
     */
    Boolean payOrder(String orderNo);

    /**
     * 取消订单
     * @param dto 取消订单请求参数
     * @return 是否成功
     */
    Boolean cancelOrder(@Valid OrderCancelDTO dto);

    /**
     * 查询订单详情
     * @param orderNo 订单号
     * @return 订单详情
     */
    OrderVO getOrderDetail(String orderNo);

    /**
     * 查询用户订单列表（分页）
     * @param dto 查询参数
     * @return 订单列表
     */
    PageVo<OrderListVO> getUserOrders(@Valid OrderQueryDTO dto);

    /**
     * 根据ID查询订单详情
     * @param orderId 订单ID
     * @return 订单详情
     */
    OrderVO getOrderDetailById(Long orderId);

    /**
     * 取消超时订单（系统调用）
     * @param order 订单对象
     */
    void cancelExpiredOrder(Order order);

    /**
     * 统计用户各状态订单数量
     * @param userId 用户ID
     * @return 订单统计信息 Map<状态码, 数量>
     */
    Map<Integer, Long> countUserOrdersByStatus(Long userId);
}

