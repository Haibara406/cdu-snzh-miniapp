package com.snzh.controller;

import com.snzh.domain.ResponseResult;
import com.snzh.domain.dto.OrderCancelDTO;
import com.snzh.domain.dto.OrderCreateDTO;
import com.snzh.domain.dto.OrderQueryDTO;
import com.snzh.domain.vo.OrderListVO;
import com.snzh.domain.vo.OrderVO;
import com.snzh.domain.vo.PageVo;
import com.snzh.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/create")
    @Operation(summary = "创建订单", description = "创建订单并返回订单号")
    public ResponseResult<String> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        String orderNo = orderService.createOrder(dto);
        return ResponseResult.success(orderNo);
    }

    @PostMapping("/pay")
    @Operation(summary = "支付订单", description = "模拟支付订单，直接更新订单状态为已支付")
    public ResponseResult<Void> payOrder(
            @Parameter(description = "订单号", required = true)
            @RequestParam String orderNo) {
        orderService.payOrder(orderNo);
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
}
