package com.snzh.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.snzh.domain.entity.Order;
import com.snzh.enums.OrderStatusEnum;
import com.snzh.mapper.OrderMapper;
import com.snzh.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author haibara
 * @description 消息通知定时任务
 * @since 2025/10/2
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationTask {

    private final OrderMapper orderMapper;
    private final INotificationService notificationService;

    /**
     * 发送游玩日期提醒
     * 每天早上9点执行，提醒明天游玩的用户
     */
    //@Scheduled(cron = "0 0 9 * * ?")
    public void sendVisitDateReminder() {
        log.info("开始执行游玩日期提醒任务");

        try {
            // 查询明天游玩的已支付订单
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            List<Order> orders = orderMapper.selectList(
                    Wrappers.lambdaQuery(Order.class)
                            .eq(Order::getVisitDate, tomorrow)
                            .in(Order::getOrderStatus, OrderStatusEnum.PAID.getCode(), OrderStatusEnum.COMPLETED.getCode())
            );

            log.info("查询到{}条明天游玩的订单", orders.size());

            int successCount = 0;
            int failCount = 0;

            for (Order order : orders) {
                try {
                    boolean success = notificationService.sendVisitDateReminder(order);
                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    log.error("发送游玩日期提醒失败，订单号：{}", order.getOrderNo(), e);
                    failCount++;
                }
            }

            log.info("游玩日期提醒任务执行完成，成功{}条，失败{}条", successCount, failCount);

        } catch (Exception e) {
            log.error("游玩日期提醒任务执行异常", e);
        }
    }

    /**
     * 发送订单即将过期提醒
     * 每10分钟执行一次，提醒10-20分钟后过期的订单
     */
    //@Scheduled(cron = "0 */10 * * * ?")
    public void sendOrderExpireReminder() {
        log.info("开始执行订单即将过期提醒任务");

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tenMinutesLater = now.plusMinutes(10);
            LocalDateTime twentyMinutesLater = now.plusMinutes(20);

            // 查询10-20分钟后过期的待支付订单
            List<Order> orders = orderMapper.selectList(
                    Wrappers.lambdaQuery(Order.class)
                            .eq(Order::getOrderStatus, OrderStatusEnum.PENDING.getCode())
                            .between(Order::getExpireTime, tenMinutesLater, twentyMinutesLater)
            );

            log.info("查询到{}条即将过期的订单", orders.size());

            int successCount = 0;
            int failCount = 0;

            for (Order order : orders) {
                try {
                    boolean success = notificationService.sendOrderExpireReminder(order);
                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    log.error("发送订单过期提醒失败，订单号：{}", order.getOrderNo(), e);
                    failCount++;
                }
            }

            log.info("订单过期提醒任务执行完成，成功{}条，失败{}条", successCount, failCount);

        } catch (Exception e) {
            log.error("订单过期提醒任务执行异常", e);
        }
    }
}