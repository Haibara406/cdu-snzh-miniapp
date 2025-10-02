package com.snzh.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snzh.constants.ErrorConst;
import com.snzh.domain.dto.OrderCancelDTO;
import com.snzh.domain.dto.OrderCreateDTO;
import com.snzh.domain.dto.OrderItemDTO;
import com.snzh.domain.dto.OrderQueryDTO;
import com.snzh.domain.entity.Order;
import com.snzh.domain.entity.OrderItem;
import com.snzh.domain.entity.ScenicTicket;
import com.snzh.domain.vo.OrderItemVO;
import com.snzh.domain.vo.OrderListVO;
import com.snzh.domain.vo.OrderVO;
import com.snzh.domain.vo.PageVo;
import com.snzh.enums.ItemTypeEnum;
import com.snzh.enums.OrderStatusEnum;
import com.snzh.enums.OrderTypeEnum;
import com.snzh.enums.RedisKeyManage;
import com.snzh.enums.StatusEnum;
import com.snzh.exceptions.OrderCancelException;
import com.snzh.exceptions.OrderNotFoundException;
import com.snzh.exceptions.OrderStatusException;
import com.snzh.exceptions.ScenicTicketNotFoundException;
import com.snzh.mapper.OrderMapper;
import com.snzh.mapper.ScenicTicketMapper;
import com.snzh.redis.RedisCache;
import com.snzh.redis.RedisKeyBuild;
import com.snzh.service.IOrderItemService;
import com.snzh.service.IOrderService;
import com.snzh.utils.PageUtil;
import com.snzh.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

/**
 * @author haibara
 * @description 订单服务实现类
 * @since 2025/10/2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final OrderMapper orderMapper;
    private final IOrderItemService orderItemService;
    private final ScenicTicketMapper scenicTicketMapper;
    private final RedisCache redisCache;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(OrderCreateDTO dto) {
        log.info("开始创建订单，用户ID：{}，游玩日期：{}", dto.getUserId(), dto.getVisitDate());

        // 1. 生成订单号
        String orderNo = generateOrderNo();

        // 2. 校验并计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemDTO item : dto.getOrderItems()) {
            // 校验门票信息（如果是门票类型）
            if (item.getItemType().equals(ItemTypeEnum.TICKET.getCode())) {
                validateTicketItem(item);
            }
            
            BigDecimal itemTotal = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        // 3. 创建订单
        Order order = Order.builder()
                .orderNo(orderNo)
                .userId(dto.getUserId())
                .phone(dto.getPhone())
                .orderType(dto.getOrderType())
                .visitDate(dto.getVisitDate())
                .totalAmount(totalAmount)
                .orderStatus(OrderStatusEnum.PENDING.getCode())
                // 30分钟后过期
                .expireTime(LocalDateTime.now().plusMinutes(30))
                .build();

        if (!save(order)) {
            throw new RuntimeException(ErrorConst.ORDER_CREATE_FAILED);
        }

        // 4. 创建订单明细
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemDTO itemDTO : dto.getOrderItems()) {
            BigDecimal itemTotal = itemDTO.getPrice().multiply(new BigDecimal(itemDTO.getQuantity()));
            OrderItem orderItem = OrderItem.builder()
                    .orderId(order.getId())
                    .itemType(itemDTO.getItemType())
                    .itemId(itemDTO.getItemId())
                    .itemName(itemDTO.getItemName())
                    .quantity(itemDTO.getQuantity())
                    .price(itemDTO.getPrice())
                    .totalAmount(itemTotal)
                    .build();
            orderItems.add(orderItem);
        }

        if (!orderItemService.batchCreate(orderItems)) {
            throw new RuntimeException(ErrorConst.ORDER_CREATE_FAILED);
        }

        log.info("订单创建成功，订单号：{}", orderNo);
        return orderNo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean payOrder(String orderNo) {
        log.info("开始支付订单，订单号：{}", orderNo);

        // 1. 查询订单
        Order order = orderMapper.selectOne(
                Wrappers.lambdaQuery(Order.class).eq(Order::getOrderNo, orderNo)
        );
        if (StringUtils.isNull(order)) {
            throw new OrderNotFoundException(ErrorConst.ORDER_NOT_FOUND);
        }

        // 2. 校验订单是否过期
        if (order.getExpireTime() != null && order.getExpireTime().isBefore(LocalDateTime.now())) {
            log.warn("订单{}已过期，自动取消", orderNo);
            // 自动取消过期订单
            cancelExpiredOrder(order);
            throw new OrderStatusException(ErrorConst.ORDER_EXPIRED);
        }

        // 3. 校验订单状态
        if (order.getOrderStatus().equals(OrderStatusEnum.PAID.getCode())) {
            throw new OrderStatusException(ErrorConst.ORDER_ALREADY_PAID);
        }
        if (order.getOrderStatus().equals(OrderStatusEnum.CANCELLED.getCode())) {
            throw new OrderStatusException(ErrorConst.ORDER_ALREADY_CANCELLED);
        }

        // 4. 更新订单状态为已支付
        order.setOrderStatus(OrderStatusEnum.PAID.getCode());
        order.setPayTime(LocalDateTime.now());
        if (!updateById(order)) {
            return false;
        }

        // 5. 更新门票销量（仅针对门票类型订单）
        if (order.getOrderType().equals(OrderTypeEnum.TICKET.getCode())) {
            updateTicketSoldCount(order.getId(), true);
        }

        // 6. 清除订单详情缓存
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.ORDER_DETAIL, orderNo));

        log.info("订单支付成功，订单号：{}", orderNo);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelOrder(OrderCancelDTO dto) {
        log.info("开始取消订单，订单号：{}，用户ID：{}", dto.getOrderNo(), dto.getUserId());

        // 1. 查询订单
        Order order = orderMapper.selectOne(
                Wrappers.lambdaQuery(Order.class).eq(Order::getOrderNo, dto.getOrderNo())
        );
        if (StringUtils.isNull(order)) {
            throw new OrderNotFoundException(ErrorConst.ORDER_NOT_FOUND);
        }

        // 2. 权限校验：只能取消自己的订单
        checkOrderPermission(order, dto.getUserId());

        // 3. 校验订单状态（只能取消已支付的订单）
        if (!order.getOrderStatus().equals(OrderStatusEnum.PAID.getCode())) {
            throw new OrderStatusException(ErrorConst.ORDER_ONLY_CANCEL_PAID);
        }

        // 4. 校验取消时间（距离游玩日期至少1天）
        LocalDate now = LocalDate.now();
        if (now.plusDays(1).isAfter(order.getVisitDate()) || now.plusDays(1).isEqual(order.getVisitDate())) {
            throw new OrderCancelException(ErrorConst.ORDER_CANCEL_TIME_ERROR);
        }

        // 5. 更新订单状态为已取消
        order.setOrderStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(dto.getCancelReason());
        if (!updateById(order)) {
            return false;
        }

        // 6. 恢复门票销量（仅针对门票类型订单）
        if (order.getOrderType().equals(OrderTypeEnum.TICKET.getCode())) {
            updateTicketSoldCount(order.getId(), false);
        }

        // 7. 清除订单详情缓存
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.ORDER_DETAIL, dto.getOrderNo()));

        log.info("订单取消成功，订单号：{}", dto.getOrderNo());
        return true;
    }

    @Override
    public OrderVO getOrderDetail(String orderNo) {
        // 1. 尝试从缓存获取
        OrderVO orderVO = redisCache.get(
                RedisKeyBuild.createKey(RedisKeyManage.ORDER_DETAIL, orderNo),
                OrderVO.class
        );

        if (StringUtils.isNotNull(orderVO)) {
            return orderVO;
        }

        // 2. 从数据库查询
        Order order = orderMapper.selectOne(
                Wrappers.lambdaQuery(Order.class).eq(Order::getOrderNo, orderNo)
        );
        if (StringUtils.isNull(order)) {
            throw new OrderNotFoundException(ErrorConst.ORDER_NOT_FOUND);
        }

        // 3. 转换为VO
        orderVO = convertToOrderVO(order);

        // 4. 缓存30分钟
        redisCache.set(
                RedisKeyBuild.createKey(RedisKeyManage.ORDER_DETAIL, orderNo),
                orderVO,
                30,
                TimeUnit.MINUTES
        );

        return orderVO;
    }

    @Override
    public PageVo<OrderListVO> getUserOrders(OrderQueryDTO dto) {
        // 构建查询条件
        IPage<Order> page = PageUtil.getPageParams(dto);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Order::getUserId, dto.getUserId())
                .eq(StringUtils.isNotNull(dto.getOrderStatus()), Order::getOrderStatus, dto.getOrderStatus())
                .eq(StringUtils.isNotNull(dto.getOrderType()), Order::getOrderType, dto.getOrderType());

        // 时间范围查询
        if (StringUtils.isNotNull(dto.getCreateTimeStart())
                && StringUtils.isNotNull(dto.getCreateTimeEnd())
                && dto.getCreateTimeStart().before(dto.getCreateTimeEnd())) {
            wrapper.between(Order::getCreateTime, dto.getCreateTimeStart(), dto.getCreateTimeEnd());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(Order::getCreateTime);

        // 分页查询
        IPage<Order> orderPage = orderMapper.selectPage(page, wrapper);

        // 转换为VO
        return PageUtil.convertPage(orderPage, this::convertToOrderListVO);
    }

    @Override
    public OrderVO getOrderDetailById(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (StringUtils.isNull(order)) {
            throw new OrderNotFoundException(ErrorConst.ORDER_NOT_FOUND);
        }
        return convertToOrderVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelExpiredOrder(Order order) {
        log.info("开始取消超时订单，订单号：{}", order.getOrderNo());

        // 1. 更新订单状态为已取消
        order.setOrderStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(ErrorConst.ORDER_TIMEOUT_CANCELLED);

        if (!updateById(order)) {
            log.error("更新超时订单状态失败，订单号：{}", order.getOrderNo());
            throw new RuntimeException("更新订单状态失败");
        }

        // 2. 恢复门票销量（仅针对门票类型订单）
        if (order.getOrderType().equals(OrderTypeEnum.TICKET.getCode())) {
            try {
                // 注意：这里不需要恢复销量，因为创建订单时还没支付，没有增加销量
                // 只有支付后才会增加销量，取消已支付订单才需要恢复
                log.debug("订单{}为待支付状态，无需恢复销量", order.getOrderNo());
            } catch (Exception e) {
                log.error("处理订单{}的门票销量异常", order.getOrderNo(), e);
            }
        }

        // 3. 清除订单详情缓存
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.ORDER_DETAIL, order.getOrderNo()));

        log.info("超时订单取消成功，订单号：{}", order.getOrderNo());
    }

    /**
     * 生成订单号
     * 格式：SNZH_ORDER + yyyyMMddHHmmss + 4位随机数
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "SNZH_ORDER" + timestamp + random;
    }

    /**
     * 更新门票销量
     * @param orderId 订单ID
     * @param isAdd true-增加销量，false-减少销量
     */
    private void updateTicketSoldCount(Long orderId, boolean isAdd) {
        List<OrderItemVO> orderItems = orderItemService.getByOrderId(orderId);
        for (OrderItemVO item : orderItems) {
            if (item.getItemType().equals(ItemTypeEnum.TICKET.getCode())) {
                ScenicTicket ticket = scenicTicketMapper.selectById(item.getItemId());
                if (StringUtils.isNull(ticket)) {
                    throw new ScenicTicketNotFoundException(ErrorConst.SCENIC_TICKET_NOT_FOUND);
                }
                int newCount = isAdd
                        ? ticket.getSoldCount() + item.getQuantity()
                        : ticket.getSoldCount() - item.getQuantity();
                // 防止负数
                ticket.setSoldCount(Math.max(0, newCount));
                scenicTicketMapper.updateById(ticket);

                // 清除门票缓存
                redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_TICKET_DETAIL, ticket.getId()));
                redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.SCENIC_TICKET_FOR_SPOT, ticket.getScenicSpotId()));
            }
        }
    }

    /**
     * 转换为OrderVO
     */
    private OrderVO convertToOrderVO(Order order) {
        OrderVO vo = BeanUtil.copyProperties(order, OrderVO.class);
        vo.setOrderTypeDesc(OrderTypeEnum.getMsg(order.getOrderType()));
        vo.setOrderStatusDesc(OrderStatusEnum.getMsg(order.getOrderStatus()));

        // 查询订单明细
        List<OrderItemVO> orderItems = orderItemService.getByOrderId(order.getId());
        vo.setOrderItems(orderItems);

        return vo;
    }

    /**
     * 转换为OrderListVO
     */
    private OrderListVO convertToOrderListVO(Order order) {
        OrderListVO vo = BeanUtil.copyProperties(order, OrderListVO.class);
        vo.setOrderTypeDesc(OrderTypeEnum.getMsg(order.getOrderType()));
        vo.setOrderStatusDesc(OrderStatusEnum.getMsg(order.getOrderStatus()));

        // 查询订单项数量
        Long itemCount = orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, order.getId())
                .count();
        vo.setItemCount(itemCount.intValue());

        return vo;
    }

    /**
     * 校验门票信息
     * @param item 订单项
     */
    private void validateTicketItem(OrderItemDTO item) {
        // 1. 查询门票是否存在
        ScenicTicket ticket = scenicTicketMapper.selectById(item.getItemId());
        if (StringUtils.isNull(ticket)) {
            log.error("门票不存在，门票ID：{}", item.getItemId());
            throw new ScenicTicketNotFoundException(ErrorConst.SCENIC_TICKET_NOT_FOUND);
        }

        // 2. 校验门票状态
        if (!ticket.getStatus().equals(StatusEnum.RUN.getCode())) {
            log.error("门票已下架，门票ID：{}", item.getItemId());
            throw new OrderStatusException(ErrorConst.TICKET_DISABLED);
        }

        // 3. 校验价格是否匹配（防止前端篡改价格）
        if (item.getPrice().compareTo(ticket.getPrice()) != 0) {
            log.error("门票价格不匹配，门票ID：{}，提交价格：{}，实际价格：{}", 
                item.getItemId(), item.getPrice(), ticket.getPrice());
            throw new OrderStatusException(ErrorConst.ORDER_PRICE_MISMATCH);
        }

        log.debug("门票校验通过，门票ID：{}，价格：{}", item.getItemId(), ticket.getPrice());
    }

    /**
     * 校验用户是否有权操作该订单
     * @param order 订单对象
     * @param userId 当前用户ID
     */
    private void checkOrderPermission(Order order, Long userId) {
        if (!order.getUserId().equals(userId)) {
            log.warn("用户{}尝试操作用户{}的订单{}，权限校验失败", 
                userId, order.getUserId(), order.getOrderNo());
            throw new OrderStatusException(ErrorConst.ORDER_NO_PERMISSION);
        }
    }

    @Override
    public Map<Integer, Long> countUserOrdersByStatus(Long userId) {
        log.info("开始统计用户订单数量，用户ID：{}", userId);

        // 1. 查询所有订单状态
        List<Order> orders = orderMapper.selectList(
                Wrappers.lambdaQuery(Order.class)
                        .eq(Order::getUserId, userId)
                        .select(Order::getOrderStatus)
        );

        // 2. 按状态分组统计
        Map<Integer, Long> statusCountMap = orders.stream()
                .collect(Collectors.groupingBy(Order::getOrderStatus, Collectors.counting()));

        // 3. 补充0的状态（前端展示需要）
        for (OrderStatusEnum statusEnum : OrderStatusEnum.values()) {
            statusCountMap.putIfAbsent(statusEnum.getCode(), 0L);
        }

        log.info("用户{}订单统计完成：{}", userId, statusCountMap);
        return statusCountMap;
    }
}
