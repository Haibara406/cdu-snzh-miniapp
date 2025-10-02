package com.snzh.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.snzh.domain.entity.AppUser;
import com.snzh.domain.entity.NotificationRecord;
import com.snzh.domain.entity.Order;
import com.snzh.domain.entity.OrderItem;
import com.snzh.enums.NotificationTemplateEnum;
import com.snzh.mapper.AppUserMapper;
import com.snzh.mapper.NotificationRecordMapper;
import com.snzh.service.INotificationService;
import com.snzh.service.IOrderItemService;
import com.snzh.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author haibara
 * @description 消息通知服务实现类
 * @since 2025/10/2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final WxMaService wxMaService;
    private final AppUserMapper appUserMapper;
    private final NotificationRecordMapper notificationRecordMapper;
    private final IOrderItemService orderItemService;

    // 从配置文件读取模板ID（需要在微信小程序后台申请）
    @Value("${snzh.template.payment-success:}")
    private String paymentSuccessTemplateId;

    @Value("${snzh.template.refund-success:}")
    private String refundSuccessTemplateId;

    @Value("${snzh.template.visit-reminder:}")
    private String visitReminderTemplateId;

    @Value("${snzh.template.expire-reminder:}")
    private String expireReminderTemplateId;

    @Value("${snzh.template.system-announcement:}")
    private String systemAnnouncementTemplateId;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Boolean sendPaymentSuccessNotice(Order order) {
        log.info("发送订单支付成功通知，订单号：{}", order.getOrderNo());

        // 1. 查询用户openid
        String openid = getUserOpenid(order.getUserId());
        if (StringUtils.isEmpty(openid)) {
            log.warn("用户{}未找到openid，无法发送通知", order.getUserId());
            return false;
        }

        // 2. 构建消息数据
        WxMaSubscribeMessage message = new WxMaSubscribeMessage();
        message.setToUser(openid);
        message.setTemplateId(paymentSuccessTemplateId);
        message.setPage("pages/order/detail?orderNo=" + order.getOrderNo());
        message.setMiniprogramState("formal");

        List<WxMaSubscribeMessage.MsgData> dataList = new ArrayList<>();
        dataList.add(new WxMaSubscribeMessage.MsgData("character_string1", order.getOrderNo())); // 订单号
        dataList.add(new WxMaSubscribeMessage.MsgData("amount2", order.getTotalAmount() + "元")); // 支付金额
        dataList.add(new WxMaSubscribeMessage.MsgData("date3", order.getPayTime().format(DATETIME_FORMATTER))); // 支付时间
        dataList.add(new WxMaSubscribeMessage.MsgData("thing4", "订单已支付成功，请在游玩日期前往景区游玩")); // 温馨提示
        message.setData(dataList);

        // 3. 发送消息
        return sendMessage(message, order.getUserId(), NotificationTemplateEnum.ORDER_PAYMENT_SUCCESS, order.getId());
    }

    @Override
    public Boolean sendRefundSuccessNotice(Order order) {
        log.info("发送订单退款成功通知，订单号：{}", order.getOrderNo());

        String openid = getUserOpenid(order.getUserId());
        if (StringUtils.isEmpty(openid)) {
            log.warn("用户{}未找到openid，无法发送通知", order.getUserId());
            return false;
        }

        WxMaSubscribeMessage message = new WxMaSubscribeMessage();
        message.setToUser(openid);
        message.setTemplateId(refundSuccessTemplateId);
        message.setPage("pages/order/detail?orderNo=" + order.getOrderNo());
        message.setMiniprogramState("formal");

        List<WxMaSubscribeMessage.MsgData> dataList = new ArrayList<>();
        dataList.add(new WxMaSubscribeMessage.MsgData("character_string1", order.getOrderNo())); // 订单号
        dataList.add(new WxMaSubscribeMessage.MsgData("amount2", order.getTotalAmount() + "元")); // 退款金额
        dataList.add(new WxMaSubscribeMessage.MsgData("date3", order.getRefundTime().format(DATETIME_FORMATTER))); // 退款时间
        dataList.add(new WxMaSubscribeMessage.MsgData("thing4", "退款已原路返回，请注意查收")); // 温馨提示
        message.setData(dataList);

        return sendMessage(message, order.getUserId(), NotificationTemplateEnum.ORDER_REFUND_SUCCESS, order.getId());
    }

    @Override
    public Boolean sendVisitDateReminder(Order order) {
        log.info("发送游玩日期提醒，订单号：{}", order.getOrderNo());

        String openid = getUserOpenid(order.getUserId());
        if (StringUtils.isEmpty(openid)) {
            log.warn("用户{}未找到openid，无法发送通知", order.getUserId());
            return false;
        }

        // 获取订单第一个景点名称
        List<OrderItem> orderItems = orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, order.getId())
                .list();
        String scenicName = orderItems.isEmpty() ? "神农竹海景区" : orderItems.get(0).getItemName();

        WxMaSubscribeMessage message = new WxMaSubscribeMessage();
        message.setToUser(openid);
        message.setTemplateId(visitReminderTemplateId);
        message.setPage("pages/order/detail?orderNo=" + order.getOrderNo());
        message.setMiniprogramState("formal");

        List<WxMaSubscribeMessage.MsgData> dataList = new ArrayList<>();
        dataList.add(new WxMaSubscribeMessage.MsgData("thing1", scenicName)); // 景点名称
        dataList.add(new WxMaSubscribeMessage.MsgData("date2", order.getVisitDate().format(DATE_FORMATTER))); // 游玩日期
        dataList.add(new WxMaSubscribeMessage.MsgData("character_string3", order.getOrderNo())); // 订单号
        dataList.add(new WxMaSubscribeMessage.MsgData("thing4", "明天就是您的游玩日期啦，祝您玩得开心！")); // 温馨提示
        message.setData(dataList);

        return sendMessage(message, order.getUserId(), NotificationTemplateEnum.VISIT_DATE_REMINDER, order.getId());
    }

    @Override
    public Boolean sendOrderExpireReminder(Order order) {
        log.info("发送订单即将过期提醒，订单号：{}", order.getOrderNo());

        String openid = getUserOpenid(order.getUserId());
        if (StringUtils.isEmpty(openid)) {
            log.warn("用户{}未找到openid，无法发送通知", order.getUserId());
            return false;
        }

        WxMaSubscribeMessage message = new WxMaSubscribeMessage();
        message.setToUser(openid);
        message.setTemplateId(expireReminderTemplateId);
        message.setPage("pages/order/detail?orderNo=" + order.getOrderNo());
        message.setMiniprogramState("formal");

        List<WxMaSubscribeMessage.MsgData> dataList = new ArrayList<>();
        dataList.add(new WxMaSubscribeMessage.MsgData("character_string1", order.getOrderNo())); // 订单号
        dataList.add(new WxMaSubscribeMessage.MsgData("date2", order.getExpireTime().format(DATETIME_FORMATTER))); // 过期时间
        dataList.add(new WxMaSubscribeMessage.MsgData("amount3", order.getTotalAmount() + "元")); // 订单金额
        dataList.add(new WxMaSubscribeMessage.MsgData("thing4", "您的订单即将过期，请尽快完成支付")); // 温馨提示
        message.setData(dataList);

        return sendMessage(message, order.getUserId(), NotificationTemplateEnum.ORDER_EXPIRE_REMINDER, order.getId());
    }

    @Override
    public Boolean sendSystemAnnouncement(Long userId, String title, String content) {
        log.info("发送系统公告通知，用户ID：{}", userId);

        String openid = getUserOpenid(userId);
        if (StringUtils.isEmpty(openid)) {
            log.warn("用户{}未找到openid，无法发送通知", userId);
            return false;
        }

        WxMaSubscribeMessage message = new WxMaSubscribeMessage();
        message.setToUser(openid);
        message.setTemplateId(systemAnnouncementTemplateId);
        message.setPage("pages/index/index");
        message.setMiniprogramState("formal");

        List<WxMaSubscribeMessage.MsgData> dataList = new ArrayList<>();
        dataList.add(new WxMaSubscribeMessage.MsgData("thing1", title)); // 公告标题
        dataList.add(new WxMaSubscribeMessage.MsgData("thing2", content)); // 公告内容
        dataList.add(new WxMaSubscribeMessage.MsgData("date3", LocalDateTime.now().format(DATETIME_FORMATTER))); // 发布时间
        message.setData(dataList);

        return sendMessage(message, userId, NotificationTemplateEnum.SYSTEM_ANNOUNCEMENT, null);
    }

    /**
     * 发送消息并记录
     *
     * @param message 消息对象
     * @param userId 用户ID
     * @param templateEnum 模板枚举
     * @param businessId 业务ID
     * @return 是否发送成功
     */
    private Boolean sendMessage(WxMaSubscribeMessage message, Long userId, NotificationTemplateEnum templateEnum, Long businessId) {
        NotificationRecord record = NotificationRecord.builder()
                .userId(userId)
                .openid(message.getToUser())
                .notificationType(templateEnum.getTemplateKey())
                .templateId(message.getTemplateId())
                .title(templateEnum.getDescription())
                .content(JSONUtil.toJsonStr(message.getData()))
                .businessId(businessId)
                .sendTime(LocalDateTime.now())
                .build();

        try {
            // 发送订阅消息
            wxMaService.getMsgService().sendSubscribeMsg(message);
            
            record.setSendStatus(1);
            log.info("消息发送成功，用户ID：{}，类型：{}", userId, templateEnum.getDescription());
            
        } catch (WxErrorException e) {
            log.error("消息发送失败，用户ID：{}，类型：{}，错误：{}", userId, templateEnum.getDescription(), e.getMessage(), e);
            record.setSendStatus(0);
            record.setFailReason(e.getMessage());
        } catch (Exception e) {
            log.error("消息发送异常，用户ID：{}，类型：{}，错误：{}", userId, templateEnum.getDescription(), e.getMessage(), e);
            record.setSendStatus(0);
            record.setFailReason(e.getMessage());
        }

        // 保存发送记录
        notificationRecordMapper.insert(record);

        return record.getSendStatus() == 1;
    }

    /**
     * 获取用户openid
     *
     * @param userId 用户ID
     * @return openid
     */
    private String getUserOpenid(Long userId) {
        AppUser user = appUserMapper.selectOne(
                Wrappers.lambdaQuery(AppUser.class)
                        .eq(AppUser::getId, userId)
                        .select(AppUser::getOpenid)
        );
        return user != null ? user.getOpenid() : null;
    }
}

