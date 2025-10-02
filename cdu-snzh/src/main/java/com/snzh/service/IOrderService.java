package com.snzh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.snzh.domain.dto.*;
import com.snzh.domain.entity.Order;
import com.snzh.domain.vo.*;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
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

    // ==================== 管理端接口 ====================

    /**
     * 管理端查询订单列表（分页）
     * @param dto 查询参数
     * @return 订单列表
     */
    PageVo<OrderListVO> getAdminOrderList(@Valid AdminOrderQueryDTO dto);

    /**
     * 管理端查询订单详情（无权限限制）
     * @param orderId 订单ID
     * @return 订单详情
     */
    OrderVO getAdminOrderDetail(Long orderId);

    /**
     * 管理端强制取消订单
     * @param dto 取消订单请求参数
     * @return 是否成功
     */
    Boolean adminCancelOrder(@Valid AdminCancelDTO dto);

    /**
     * 管理端退款处理
     * @param dto 退款请求参数
     * @return 是否成功
     */
    Boolean adminRefund(@Valid AdminRefundDTO dto);

    /**
     * 管理端修改订单信息
     * @param dto 修改订单请求参数
     * @return 是否成功
     */
    Boolean adminUpdateOrder(@Valid AdminOrderUpdateDTO dto);

    /**
     * 管理端手动完成订单
     * @param orderId 订单ID
     * @return 是否成功
     */
    Boolean adminCompleteOrder(Long orderId);

    /**
     * 管理端批量操作
     * @param dto 批量操作请求参数
     * @return 操作结果 Map<订单ID, 是否成功>
     */
    Map<Long, Boolean> adminBatchOperation(@Valid BatchOperationDTO dto);

    /**
     * 获取数据看板
     * @return 数据看板信息
     */
    OrderDashboardVO getDashboard();

    /**
     * 获取订单趋势统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 订单趋势列表
     */
    List<OrderTrendVO> getOrderTrend(LocalDate startDate, LocalDate endDate);

    /**
     * 获取订单状态分布统计
     * @return 状态分布 Map<状态码, 数量>
     */
    Map<Integer, Long> getStatusDistribution();

    /**
     * 获取订单类型分布统计
     * @return 类型分布 Map<类型码, 数量>
     */
    Map<Integer, Long> getTypeDistribution();

    /**
     * 获取销售额统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 销售额趋势列表
     */
    List<OrderTrendVO> getRevenueStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 获取转化率统计
     * @return 转化率统计信息
     */
    ConversionStatisticsVO getConversionStatistics();

    /**
     * 获取用户消费统计
     * @return 用户消费统计信息
     */
    UserStatisticsVO getUserStatistics();

    /**
     * 获取快捷筛选数据
     * @return 快捷筛选信息
     */
    QuickFilterVO getQuickFilter();

    /**
     * 获取异常订单列表
     * @return 异常订单列表
     */
    List<OrderListVO> getAbnormalOrders();
}

