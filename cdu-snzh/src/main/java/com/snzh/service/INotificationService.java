package com.snzh.service;

import com.snzh.domain.entity.Order;

/**
 * @author haibara
 * @description 消息通知服务接口
 * @since 2025/10/2
 */
public interface INotificationService {

    /**
     * 发送订单支付成功通知
     *
     * @param order 订单信息
     * @return 是否发送成功
     */
    Boolean sendPaymentSuccessNotice(Order order);

    /**
     * 发送订单退款成功通知
     *
     * @param order 订单信息
     * @return 是否发送成功
     */
    Boolean sendRefundSuccessNotice(Order order);

    /**
     * 发送游玩日期提醒
     *
     * @param order 订单信息
     * @return 是否发送成功
     */
    Boolean sendVisitDateReminder(Order order);

    /**
     * 发送订单即将过期提醒
     *
     * @param order 订单信息
     * @return 是否发送成功
     */
    Boolean sendOrderExpireReminder(Order order);

    /**
     * 发送系统公告通知
     *
     * @param userId 用户ID
     * @param title  公告标题
     * @param content 公告内容
     * @return 是否发送成功
     */
    Boolean sendSystemAnnouncement(Long userId, String title, String content);
}

