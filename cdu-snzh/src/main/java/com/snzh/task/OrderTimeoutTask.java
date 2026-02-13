package com.snzh.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.snzh.constants.BusinessConst;
import com.snzh.domain.entity.Order;
import com.snzh.enums.OrderStatusEnum;
import com.snzh.mapper.OrderMapper;
import com.snzh.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author haibara
 * @description 订单超时处理定时任务
 * @since 2025/10/2
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderMapper orderMapper;
    private final IOrderService orderService;

    /**
     * 定时扫描并取消超时订单
     * 每5分钟执行一次
     */
    //@Scheduled(cron = "0 */5 * * * ?")
    public void cancelExpiredOrders() {
        log.info("========== 开始扫描超时订单 ==========");
        long startTime = System.currentTimeMillis();

        try {
            // 1. 查询超时的待支付订单
            List<Order> expiredOrders = orderMapper.selectList(
                    Wrappers.lambdaQuery(Order.class)
                            .eq(Order::getOrderStatus, OrderStatusEnum.PENDING.getCode())
                            .lt(Order::getExpireTime, LocalDateTime.now())
                            .orderByAsc(Order::getExpireTime)
                            .last("LIMIT " + BusinessConst.Limit.DEFAULT_BATCH_LIMIT) // 一次最多处理限制数量
            );

            if (expiredOrders.isEmpty()) {
                log.info("未发现超时订单");
                return;
            }

            log.info("发现{}条超时订单，开始处理", expiredOrders.size());

            // 2. 批量取消订单
            int successCount = 0;
            int failCount = 0;

            for (Order order : expiredOrders) {
                try {
                    orderService.cancelExpiredOrder(order);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.error("取消超时订单失败，订单号：{}，错误：{}", order.getOrderNo(), e.getMessage(), e);
                }
            }

            long endTime = System.currentTimeMillis();
            log.info("========== 超时订单处理完成 ==========");
            log.info("总共处理：{}条，成功：{}条，失败：{}条，耗时：{}ms",
                    expiredOrders.size(), successCount, failCount, (endTime - startTime));

        } catch (Exception e) {
            log.error("扫描超时订单异常", e);
        }
    }

    /**
     * 定时扫描并完成游玩日期已过的订单
     * 每天凌晨1点执行
     */
    //@Scheduled(cron = "0 0 1 * * ?")
    public void completeExpiredVisitOrders() {
        log.info("========== 开始扫描游玩日期已过订单 ==========");
        long startTime = System.currentTimeMillis();

        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);

            // 1. 查询游玩日期已过的已支付订单
            List<Order> expiredVisitOrders = orderMapper.selectList(
                    Wrappers.lambdaQuery(Order.class)
                            .eq(Order::getOrderStatus, OrderStatusEnum.PAID.getCode())
                            .le(Order::getVisitDate, yesterday)
                            .orderByAsc(Order::getVisitDate)
                            // 一次最多处理限制数量
                            .last("LIMIT " + BusinessConst.Limit.DEFAULT_BATCH_LIMIT)
            );

            if (expiredVisitOrders.isEmpty()) {
                log.info("未发现需要完成的订单");
                return;
            }

            log.info("发现{}条游玩日期已过订单，开始自动完成", expiredVisitOrders.size());

            // 2. 批量完成订单
            int successCount = 0;
            int failCount = 0;

            for (Order order : expiredVisitOrders) {
                try {
                    // 调用管理端完成订单方法，统一处理逻辑和缓存清理
                    orderService.adminCompleteOrder(order.getId());
                    
                    log.info("订单自动完成，订单号：{}，游玩日期：{}", order.getOrderNo(), order.getVisitDate());
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.error("完成订单失败，订单号：{}，错误：{}", order.getOrderNo(), e.getMessage(), e);
                }
            }

            long endTime = System.currentTimeMillis();
            log.info("========== 游玩日期已过订单处理完成 ==========");
            log.info("总共处理：{}条，成功：{}条，失败：{}条，耗时：{}ms",
                    expiredVisitOrders.size(), successCount, failCount, (endTime - startTime));

        } catch (Exception e) {
            log.error("扫描游玩日期已过订单异常", e);
        }
    }
}