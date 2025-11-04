package com.snzh.controller;

import com.snzh.annotation.RequireAdmin;
import com.snzh.domain.ResponseResult;
import com.snzh.domain.dto.*;
import com.snzh.domain.vo.*;
import com.snzh.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author haibara
 * @description 订单Controller
 * @since 2025/10/2
 */
@Slf4j
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Tag(name = "订单API", description = "订单相关接口")
public class OrderController {

    private final IOrderService orderService;

    // ==================== 用户端接口 ====================

    @PostMapping("/create")
    @Operation(summary = "创建订单", description = "创建订单并返回订单号")
    public ResponseResult<String> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        String orderNo = orderService.createOrder(dto);
        return ResponseResult.success(orderNo);
    }

    @PostMapping("/pay")
    @Operation(summary = "支付订单", description = "模拟支付订单，直接更新订单状态为已支付")
    public ResponseResult<Void> payOrder(@Valid @RequestBody OrderPayDTO dto) {
        orderService.payOrder(dto.getOrderNo());
        return ResponseResult.success();
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消订单", description = "取消已支付的订单，需满足距离游玩日期至少1天")
    public ResponseResult<Void> cancelOrder(@Valid @RequestBody OrderCancelDTO dto) {
        orderService.cancelOrder(dto);
        return ResponseResult.success();
    }

    @GetMapping("/detail")
    @Operation(summary = "查询订单详情", description = "根据订单号查询订单详情，包含订单明细")
    public ResponseResult<OrderVO> getOrderDetail(
            @Parameter(description = "订单号", required = true)
            @RequestParam String orderNo) {
        OrderVO orderVO = orderService.getOrderDetail(orderNo);
        return ResponseResult.success(orderVO);
    }

    @GetMapping("/list")
    @Operation(summary = "查询用户订单列表", description = "分页查询用户订单列表，支持按状态和类型筛选")
    public ResponseResult<PageVo<OrderListVO>> getUserOrders(@Valid OrderQueryDTO dto) {
        PageVo<OrderListVO> orders = orderService.getUserOrders(dto);
        return ResponseResult.success(orders);
    }

    @GetMapping("/detail/{orderId}")
    @Operation(summary = "根据ID查询订单详情", description = "根据订单ID查询订单详情")
    public ResponseResult<OrderVO> getOrderDetailById(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId) {
        OrderVO orderVO = orderService.getOrderDetailById(orderId);
        return ResponseResult.success(orderVO);
    }

    @GetMapping("/count")
    @Operation(summary = "统计用户订单数量", description = "按状态统计用户各类型订单数量，返回Map<状态码, 数量>")
    public ResponseResult<Map<Integer, Long>> countUserOrders(
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId) {
        Map<Integer, Long> statusCountMap = orderService.countUserOrdersByStatus(userId);
        return ResponseResult.success(statusCountMap);
    }

    // ==================== 管理端接口 ====================

    @RequireAdmin
    @GetMapping("/admin/list")
    @Operation(summary = "【管理端】查询订单列表", description = "管理端分页查询订单列表，支持多条件筛选")
    public ResponseResult<PageVo<OrderListVO>> getAdminOrderList(@Valid AdminOrderQueryDTO dto) {
        PageVo<OrderListVO> orders = orderService.getAdminOrderList(dto);
        return ResponseResult.success(orders);
    }

    @RequireAdmin
    @GetMapping("/admin/detail/{orderId}")
    @Operation(summary = "【管理端】查询订单详情", description = "管理端查询订单详情，无权限限制")
    public ResponseResult<OrderVO> getAdminOrderDetail(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId) {
        OrderVO orderVO = orderService.getAdminOrderDetail(orderId);
        return ResponseResult.success(orderVO);
    }

    @RequireAdmin
    @PostMapping("/admin/cancel")
    @Operation(summary = "【管理端】强制取消订单", description = "管理端强制取消订单，不受日期限制")
    public ResponseResult<Void> adminCancelOrder(@Valid @RequestBody AdminCancelDTO dto) {
        orderService.adminCancelOrder(dto);
        return ResponseResult.success();
    }

    @RequireAdmin
    @PostMapping("/admin/refund")
    @Operation(summary = "【管理端】退款处理", description = "管理端处理订单退款")
    public ResponseResult<Void> adminRefund(@Valid @RequestBody AdminRefundDTO dto) {
        orderService.adminRefund(dto);
        return ResponseResult.success();
    }

    @RequireAdmin
    @PutMapping("/admin/update")
    @Operation(summary = "【管理端】修改订单信息", description = "管理端修改订单基本信息")
    public ResponseResult<Void> adminUpdateOrder(@Valid @RequestBody AdminOrderUpdateDTO dto) {
        orderService.adminUpdateOrder(dto);
        return ResponseResult.success();
    }

    @RequireAdmin
    @PostMapping("/admin/complete/{orderId}")
    @Operation(summary = "【管理端】手动完成订单", description = "管理端手动完成订单")
    public ResponseResult<Void> adminCompleteOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId) {
        orderService.adminCompleteOrder(orderId);
        return ResponseResult.success();
    }

    @RequireAdmin
    @PostMapping("/admin/batch")
    @Operation(summary = "【管理端】批量操作", description = "管理端批量操作订单（取消/退款/完成）")
    public ResponseResult<Map<Long, Boolean>> adminBatchOperation(@Valid @RequestBody BatchOperationDTO dto) {
        Map<Long, Boolean> resultMap = orderService.adminBatchOperation(dto);
        return ResponseResult.success(resultMap);
    }

    @RequireAdmin
    @GetMapping("/admin/dashboard")
    @Operation(summary = "【管理端】数据看板", description = "获取订单数据看板信息")
    public ResponseResult<OrderDashboardVO> getDashboard() {
        OrderDashboardVO dashboard = orderService.getDashboard();
        return ResponseResult.success(dashboard);
    }

    @RequireAdmin
    @GetMapping("/admin/statistics/trend")
    @Operation(summary = "【管理端】订单趋势统计", description = "获取订单趋势统计数据")
    public ResponseResult<List<OrderTrendVO>> getOrderTrend(
            @Parameter(description = "开始日期", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<OrderTrendVO> trendList = orderService.getOrderTrend(startDate, endDate);
        return ResponseResult.success(trendList);
    }

    @RequireAdmin
    @GetMapping("/admin/statistics/status")
    @Operation(summary = "【管理端】订单状态分布", description = "获取订单状态分布统计")
    public ResponseResult<Map<Integer, Long>> getStatusDistribution() {
        Map<Integer, Long> statusMap = orderService.getStatusDistribution();
        return ResponseResult.success(statusMap);
    }

    @RequireAdmin
    @GetMapping("/admin/statistics/type")
    @Operation(summary = "【管理端】订单类型分布", description = "获取订单类型分布统计")
    public ResponseResult<Map<Integer, Long>> getTypeDistribution() {
        Map<Integer, Long> typeMap = orderService.getTypeDistribution();
        return ResponseResult.success(typeMap);
    }

    @RequireAdmin
    @GetMapping("/admin/statistics/revenue")
    @Operation(summary = "【管理端】销售额统计", description = "获取销售额统计数据")
    public ResponseResult<List<OrderTrendVO>> getRevenueStatistics(
            @Parameter(description = "开始日期", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<OrderTrendVO> revenueList = orderService.getRevenueStatistics(startDate, endDate);
        return ResponseResult.success(revenueList);
    }

    @RequireAdmin
    @GetMapping("/admin/statistics/conversion")
    @Operation(summary = "【管理端】转化率统计", description = "获取订单转化率统计信息")
    public ResponseResult<ConversionStatisticsVO> getConversionStatistics() {
        ConversionStatisticsVO statistics = orderService.getConversionStatistics();
        return ResponseResult.success(statistics);
    }

    @RequireAdmin
    @GetMapping("/admin/statistics/user")
    @Operation(summary = "【管理端】用户消费统计", description = "获取用户消费统计信息")
    public ResponseResult<UserStatisticsVO> getUserStatistics() {
        UserStatisticsVO statistics = orderService.getUserStatistics();
        return ResponseResult.success(statistics);
    }

    @RequireAdmin
    @GetMapping("/admin/quick-filter")
    @Operation(summary = "【管理端】快捷筛选数据", description = "获取快捷筛选数据")
    public ResponseResult<QuickFilterVO> getQuickFilter() {
        QuickFilterVO quickFilter = orderService.getQuickFilter();
        return ResponseResult.success(quickFilter);
    }

    @RequireAdmin
    @GetMapping("/admin/abnormal-list")
    @Operation(summary = "【管理端】异常订单列表", description = "获取异常订单列表（超时未支付）")
    public ResponseResult<List<OrderListVO>> getAbnormalOrders() {
        List<OrderListVO> abnormalOrders = orderService.getAbnormalOrders();
        return ResponseResult.success(abnormalOrders);
    }
}
