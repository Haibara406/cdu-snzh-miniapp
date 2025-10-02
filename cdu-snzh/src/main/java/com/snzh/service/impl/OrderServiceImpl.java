package com.snzh.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snzh.constants.ErrorConst;
import com.snzh.domain.dto.*;
import com.snzh.domain.entity.Order;
import com.snzh.domain.entity.OrderItem;
import com.snzh.domain.entity.ScenicTicket;
import com.snzh.domain.vo.*;
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
import java.math.RoundingMode;
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
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        if (!order.getVisitDate().isAfter(tomorrow)) {
            throw new OrderCancelException(ErrorConst.ORDER_CANCEL_TIME_ERROR);
        }

        // 5. 更新订单状态为已退款（已支付订单取消即退款）
        order.setOrderStatus(OrderStatusEnum.REFUNDED.getCode());
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(dto.getCancelReason());
        order.setRefundTime(LocalDateTime.now());
        if (!updateById(order)) {
            return false;
        }

        // 6. 恢复门票销量（已支付订单退款需要恢复销量）
        if (order.getOrderType().equals(OrderTypeEnum.TICKET.getCode())) {
            updateTicketSoldCount(order.getId(), false);
        }

        // 7. 清除订单详情缓存
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.ORDER_DETAIL, dto.getOrderNo()));

        log.info("订单退款成功，订单号：{}", dto.getOrderNo());
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

        // 2. 注意：待支付订单取消不需要恢复销量，因为创建订单时还没支付，没有增加销量
        //    只有支付后才会增加销量，取消已支付订单才需要恢复

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

    // ==================== 管理端接口实现 ====================

    @Override
    public PageVo<OrderListVO> getAdminOrderList(AdminOrderQueryDTO dto) {
        log.info("管理端查询订单列表，参数：{}", dto);

        IPage<Order> page = PageUtil.getPageParams(dto);

        // 构建查询条件
        LambdaQueryWrapper<Order> wrapper = Wrappers.lambdaQuery(Order.class);

        // 订单号模糊搜索
        if (StringUtils.isNotEmpty(dto.getOrderNo())) {
            wrapper.like(Order::getOrderNo, dto.getOrderNo());
        }

        // 用户ID
        if (dto.getUserId() != null) {
            wrapper.eq(Order::getUserId, dto.getUserId());
        }

        // 手机号模糊搜索
        if (StringUtils.isNotEmpty(dto.getPhone())) {
            wrapper.like(Order::getPhone, dto.getPhone());
        }

        // 订单状态
        if (dto.getOrderStatus() != null) {
            wrapper.eq(Order::getOrderStatus, dto.getOrderStatus());
        }

        // 订单类型
        if (dto.getOrderType() != null) {
            wrapper.eq(Order::getOrderType, dto.getOrderType());
        }

        // 下单时间范围
        if (dto.getCreateTimeStart() != null) {
            wrapper.ge(Order::getCreateTime, dto.getCreateTimeStart());
        }
        if (dto.getCreateTimeEnd() != null) {
            wrapper.le(Order::getCreateTime, dto.getCreateTimeEnd());
        }

        // 游玩日期范围
        if (dto.getVisitDateStart() != null) {
            wrapper.ge(Order::getVisitDate, dto.getVisitDateStart());
        }
        if (dto.getVisitDateEnd() != null) {
            wrapper.le(Order::getVisitDate, dto.getVisitDateEnd());
        }

        // 金额范围
        if (dto.getMinAmount() != null) {
            wrapper.ge(Order::getTotalAmount, dto.getMinAmount());
        }
        if (dto.getMaxAmount() != null) {
            wrapper.le(Order::getTotalAmount, dto.getMaxAmount());
        }

        // 快捷筛选
        if (StringUtils.isNotEmpty(dto.getQuickFilter())) {
            applyQuickFilter(wrapper, dto.getQuickFilter());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(Order::getCreateTime);

        // 分页查询
        IPage<Order> orderPage = orderMapper.selectPage(page, wrapper);
        PageVo<OrderListVO> orderListVOPageVo = PageUtil.convertPage(orderPage, order -> BeanUtil.copyProperties(order, OrderListVO.class));
        log.info("管理端查询订单列表完成，共{}条", orderListVOPageVo.getTotalSize());
        return orderListVOPageVo;
    }

    @Override
    public OrderVO getAdminOrderDetail(Long orderId) {
        log.info("管理端查询订单详情，订单ID：{}", orderId);

        Order order = getById(orderId);
        if (StringUtils.isNull(order)) {
            throw new OrderNotFoundException(ErrorConst.ORDER_NOT_FOUND);
        }

        return convertToOrderVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean adminCancelOrder(AdminCancelDTO dto) {
        log.info("管理端取消订单，订单号：{}", dto.getOrderNo());

        // 1. 查询订单
        Order order = orderMapper.selectOne(
                Wrappers.lambdaQuery(Order.class).eq(Order::getOrderNo, dto.getOrderNo())
        );
        if (StringUtils.isNull(order)) {
            throw new OrderNotFoundException(ErrorConst.ORDER_NOT_FOUND);
        }

        // 2. 校验订单状态（只能取消待支付和已支付的订单）
        if (order.getOrderStatus().equals(OrderStatusEnum.CANCELLED.getCode()) ||
                order.getOrderStatus().equals(OrderStatusEnum.REFUNDED.getCode()) ||
                order.getOrderStatus().equals(OrderStatusEnum.COMPLETED.getCode())) {
            throw new OrderStatusException("该订单状态不允许取消");
        }

        // 3. 判断订单原始状态
        boolean isPaid = order.getOrderStatus().equals(OrderStatusEnum.PAID.getCode());

        // 4. 根据订单状态更新：待支付→已取消，已支付→已退款
        if (isPaid) {
            order.setOrderStatus(OrderStatusEnum.REFUNDED.getCode());
            order.setRefundTime(LocalDateTime.now());
        } else {
            order.setOrderStatus(OrderStatusEnum.CANCELLED.getCode());
        }
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(dto.getCancelReason());
        if (!updateById(order)) {
            return false;
        }

        // 5. 如果是已支付订单，需要恢复门票销量（待支付订单从未增加销量，无需恢复）
        if (isPaid && order.getOrderType().equals(OrderTypeEnum.TICKET.getCode())) {
            updateTicketSoldCount(order.getId(), false);
        }

        // 6. 清除订单详情缓存
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.ORDER_DETAIL, dto.getOrderNo()));

        log.info("管理端取消订单成功，订单号：{}，新状态：{}", dto.getOrderNo(), 
                OrderStatusEnum.getMsg(order.getOrderStatus()));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean adminRefund(AdminRefundDTO dto) {
        log.info("管理端退款处理，订单号：{}", dto.getOrderNo());

        // 1. 查询订单
        Order order = orderMapper.selectOne(
                Wrappers.lambdaQuery(Order.class).eq(Order::getOrderNo, dto.getOrderNo())
        );
        if (StringUtils.isNull(order)) {
            throw new OrderNotFoundException(ErrorConst.ORDER_NOT_FOUND);
        }

        // 2. 校验订单状态（只能退款已支付的订单，已取消是待支付订单的状态）
        if (!order.getOrderStatus().equals(OrderStatusEnum.PAID.getCode())) {
            throw new OrderStatusException("只有已支付的订单才能退款");
        }

        // 3. 校验退款金额
        if (dto.getRefundAmount().compareTo(order.getTotalAmount()) > 0) {
            throw new OrderStatusException("退款金额不能超过订单总金额");
        }

        // 4. 更新订单状态为已退款
        order.setOrderStatus(OrderStatusEnum.REFUNDED.getCode());
        order.setRefundTime(LocalDateTime.now());
        order.setCancelReason(dto.getRefundReason());
        if (!updateById(order)) {
            return false;
        }

        // 5. 恢复门票销量（已支付订单退款需要恢复销量）
        if (order.getOrderType().equals(OrderTypeEnum.TICKET.getCode())) {
            updateTicketSoldCount(order.getId(), false);
        }

        // 6. 清除订单详情缓存
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.ORDER_DETAIL, dto.getOrderNo()));

        log.info("管理端退款处理成功，订单号：{}，退款金额：{}", dto.getOrderNo(), dto.getRefundAmount());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean adminUpdateOrder(AdminOrderUpdateDTO dto) {
        log.info("管理端修改订单信息，订单ID：{}", dto.getOrderId());

        // 1. 查询订单
        Order order = getById(dto.getOrderId());
        if (StringUtils.isNull(order)) {
            throw new OrderNotFoundException(ErrorConst.ORDER_NOT_FOUND);
        }

        // 2. 更新订单信息
        boolean updated = false;
        if (StringUtils.isNotEmpty(dto.getPhone())) {
            order.setPhone(dto.getPhone());
            updated = true;
        }
        if (dto.getVisitDate() != null) {
            order.setVisitDate(dto.getVisitDate());
            updated = true;
        }

        if (!updated) {
            return true;
        }

        if (!updateById(order)) {
            return false;
        }

        // 3. 清除订单详情缓存
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.ORDER_DETAIL, order.getOrderNo()));

        log.info("管理端修改订单信息成功，订单ID：{}，修改原因：{}", dto.getOrderId(), dto.getUpdateReason());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean adminCompleteOrder(Long orderId) {
        log.info("管理端手动完成订单，订单ID：{}", orderId);

        // 1. 查询订单
        Order order = getById(orderId);
        if (StringUtils.isNull(order)) {
            throw new OrderNotFoundException(ErrorConst.ORDER_NOT_FOUND);
        }

        // 2. 校验订单状态（只能完成已支付的订单）
        if (!order.getOrderStatus().equals(OrderStatusEnum.PAID.getCode())) {
            throw new OrderStatusException("只能完成已支付的订单");
        }

        // 3. 更新订单状态为已完成
        order.setOrderStatus(OrderStatusEnum.COMPLETED.getCode());
        order.setCompleteTime(LocalDateTime.now());
        if (!updateById(order)) {
            return false;
        }

        // 4. 清除订单详情缓存
        redisCache.del(RedisKeyBuild.createKey(RedisKeyManage.ORDER_DETAIL, order.getOrderNo()));

        log.info("管理端手动完成订单成功，订单ID：{}", orderId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<Long, Boolean> adminBatchOperation(BatchOperationDTO dto) {
        log.info("管理端批量操作，操作类型：{}，订单数量：{}", dto.getOperation(), dto.getOrderIds().size());

        Map<Long, Boolean> resultMap = new HashMap<>();

        for (Long orderId : dto.getOrderIds()) {
            try {
                boolean success = false;
                switch (dto.getOperation()) {
                    case "cancel":
                        Order order = getById(orderId);
                        if (order != null) {
                            AdminCancelDTO cancelDTO = new AdminCancelDTO();
                            cancelDTO.setOrderNo(order.getOrderNo());
                            cancelDTO.setCancelReason(dto.getReason());
                            success = adminCancelOrder(cancelDTO);
                        }
                        break;
                    case "refund":
                        order = getById(orderId);
                        if (order != null) {
                            AdminRefundDTO refundDTO = new AdminRefundDTO();
                            refundDTO.setOrderNo(order.getOrderNo());
                            refundDTO.setRefundAmount(order.getTotalAmount());
                            refundDTO.setRefundReason(dto.getReason());
                            success = adminRefund(refundDTO);
                        }
                        break;
                    case "complete":
                        success = adminCompleteOrder(orderId);
                        break;
                    default:
                        log.warn("不支持的操作类型：{}", dto.getOperation());
                }
                resultMap.put(orderId, success);
            } catch (Exception e) {
                log.error("批量操作失败，订单ID：{}，错误：{}", orderId, e.getMessage(), e);
                resultMap.put(orderId, false);
            }
        }

        log.info("管理端批量操作完成，成功{}条，失败{}条",
                resultMap.values().stream().filter(v -> v).count(),
                resultMap.values().stream().filter(v -> !v).count());

        return resultMap;
    }

    @Override
    public OrderDashboardVO getDashboard() {
        log.info("获取数据看板");

        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime tomorrow = today.plusDays(1);
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        // ========== 今日数据 ==========
        Integer todayTotal = Math.toIntExact(lambdaQuery()
                .between(Order::getCreateTime, today, tomorrow)
                .count());

        Integer todayPending = Math.toIntExact(lambdaQuery()
                .between(Order::getCreateTime, today, tomorrow)
                .eq(Order::getOrderStatus, OrderStatusEnum.PENDING.getCode())
                .count());

        Integer todayPaid = Math.toIntExact(lambdaQuery()
                .between(Order::getCreateTime, today, tomorrow)
                .eq(Order::getOrderStatus, OrderStatusEnum.PAID.getCode())
                .count());

        Integer todayCancelled = Math.toIntExact(lambdaQuery()
                .between(Order::getCreateTime, today, tomorrow)
                .eq(Order::getOrderStatus, OrderStatusEnum.CANCELLED.getCode())
                .count());

        Integer todayRefunded = Math.toIntExact(lambdaQuery()
                .between(Order::getCreateTime, today, tomorrow)
                .eq(Order::getOrderStatus, OrderStatusEnum.REFUNDED.getCode())
                .count());

        Integer todayCompleted = Math.toIntExact(lambdaQuery()
                .between(Order::getCreateTime, today, tomorrow)
                .eq(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode())
                .count());

        // 今日销售额（已支付+已完成）
        BigDecimal todayRevenue = orderMapper.selectList(
                        Wrappers.lambdaQuery(Order.class)
                                .between(Order::getCreateTime, today, tomorrow)
                                .in(Order::getOrderStatus, OrderStatusEnum.PAID.getCode(), OrderStatusEnum.COMPLETED.getCode())
                ).stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ========== 本月数据 ==========
        Integer monthTotal = Math.toIntExact(lambdaQuery()
                .ge(Order::getCreateTime, monthStart)
                .count());

        Integer monthPending = Math.toIntExact(lambdaQuery()
                .ge(Order::getCreateTime, monthStart)
                .eq(Order::getOrderStatus, OrderStatusEnum.PENDING.getCode())
                .count());

        Integer monthPaid = Math.toIntExact(lambdaQuery()
                .ge(Order::getCreateTime, monthStart)
                .eq(Order::getOrderStatus, OrderStatusEnum.PAID.getCode())
                .count());

        Integer monthCancelled = Math.toIntExact(lambdaQuery()
                .ge(Order::getCreateTime, monthStart)
                .eq(Order::getOrderStatus, OrderStatusEnum.CANCELLED.getCode())
                .count());

        Integer monthRefunded = Math.toIntExact(lambdaQuery()
                .ge(Order::getCreateTime, monthStart)
                .eq(Order::getOrderStatus, OrderStatusEnum.REFUNDED.getCode())
                .count());

        Integer monthCompleted = Math.toIntExact(lambdaQuery()
                .ge(Order::getCreateTime, monthStart)
                .eq(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode())
                .count());

        // 本月销售额（已支付+已完成）
        BigDecimal monthRevenue = orderMapper.selectList(
                        Wrappers.lambdaQuery(Order.class)
                                .ge(Order::getCreateTime, monthStart)
                                .in(Order::getOrderStatus, OrderStatusEnum.PAID.getCode(), OrderStatusEnum.COMPLETED.getCode())
                ).stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ========== 异常订单 ==========
        Integer abnormalOrders = Math.toIntExact(lambdaQuery()
                .eq(Order::getOrderStatus, OrderStatusEnum.PENDING.getCode())
                .lt(Order::getExpireTime, LocalDateTime.now())
                .count());

        return OrderDashboardVO.builder()
                .todayTotal(todayTotal)
                .todayPending(todayPending)
                .todayPaid(todayPaid)
                .todayCancelled(todayCancelled)
                .todayRefunded(todayRefunded)
                .todayCompleted(todayCompleted)
                .todayRevenue(todayRevenue)
                .monthTotal(monthTotal)
                .monthPending(monthPending)
                .monthPaid(monthPaid)
                .monthCancelled(monthCancelled)
                .monthRefunded(monthRefunded)
                .monthCompleted(monthCompleted)
                .monthRevenue(monthRevenue)
                .abnormalOrders(abnormalOrders)
                .build();
    }

    @Override
    public List<OrderTrendVO> getOrderTrend(LocalDate startDate, LocalDate endDate) {
        log.info("获取订单趋势统计，开始日期：{}，结束日期：{}", startDate, endDate);

        List<Order> orders = orderMapper.selectList(
                Wrappers.lambdaQuery(Order.class)
                        .ge(Order::getCreateTime, startDate.atStartOfDay())
                        .le(Order::getCreateTime, endDate.atTime(23, 59, 59))
        );

        // 按日期分组统计
        Map<LocalDate, List<Order>> dateOrderMap = orders.stream()
                .collect(Collectors.groupingBy(order -> order.getCreateTime().toLocalDate()));

        List<OrderTrendVO> trendList = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            List<Order> dayOrders = dateOrderMap.getOrDefault(currentDate, Collections.emptyList());

            int orderCount = dayOrders.size();
            int paidCount = (int) dayOrders.stream()
                    .filter(o -> o.getOrderStatus().equals(OrderStatusEnum.PAID.getCode()) ||
                            o.getOrderStatus().equals(OrderStatusEnum.COMPLETED.getCode()))
                    .count();

            BigDecimal revenue = dayOrders.stream()
                    .filter(o -> o.getOrderStatus().equals(OrderStatusEnum.PAID.getCode()) ||
                            o.getOrderStatus().equals(OrderStatusEnum.COMPLETED.getCode()))
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            trendList.add(OrderTrendVO.builder()
                    .date(currentDate.toString())
                    .orderCount(orderCount)
                    .paidCount(paidCount)
                    .revenue(revenue)
                    .build());

            currentDate = currentDate.plusDays(1);
        }

        return trendList;
    }

    @Override
    public Map<Integer, Long> getStatusDistribution() {
        log.info("获取订单状态分布统计");

        List<Order> orders = orderMapper.selectList(
                Wrappers.lambdaQuery(Order.class).select(Order::getOrderStatus)
        );

        Map<Integer, Long> statusMap = orders.stream()
                .collect(Collectors.groupingBy(Order::getOrderStatus, Collectors.counting()));

        // 补充0的状态
        for (OrderStatusEnum statusEnum : OrderStatusEnum.values()) {
            statusMap.putIfAbsent(statusEnum.getCode(), 0L);
        }

        return statusMap;
    }

    @Override
    public Map<Integer, Long> getTypeDistribution() {
        log.info("获取订单类型分布统计");

        List<Order> orders = orderMapper.selectList(
                Wrappers.lambdaQuery(Order.class).select(Order::getOrderType)
        );

        Map<Integer, Long> typeMap = orders.stream()
                .collect(Collectors.groupingBy(Order::getOrderType, Collectors.counting()));

        // 补充0的类型
        for (OrderTypeEnum typeEnum : OrderTypeEnum.values()) {
            typeMap.putIfAbsent(typeEnum.getCode(), 0L);
        }

        return typeMap;
    }

    @Override
    public List<OrderTrendVO> getRevenueStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("获取销售额统计，开始日期：{}，结束日期：{}", startDate, endDate);

        // 复用订单趋势统计的数据
        return getOrderTrend(startDate, endDate);
    }

    @Override
    public ConversionStatisticsVO getConversionStatistics() {
        log.info("获取转化率统计");

        // 总订单数
        Long totalOrders = lambdaQuery().count();

        // 已支付订单数
        Long paidOrders = lambdaQuery()
                .in(Order::getOrderStatus, OrderStatusEnum.PAID.getCode(), OrderStatusEnum.COMPLETED.getCode())
                .count();

        // 已取消订单数
        Long cancelledOrders = lambdaQuery()
                .eq(Order::getOrderStatus, OrderStatusEnum.CANCELLED.getCode())
                .count();

        // 已退款订单数
        Long refundedOrders = lambdaQuery()
                .eq(Order::getOrderStatus, OrderStatusEnum.REFUNDED.getCode())
                .count();

        // 已完成订单数
        Long completedOrders = lambdaQuery()
                .eq(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode())
                .count();

        // 计算转化率
        BigDecimal paymentRate = totalOrders > 0
                ? BigDecimal.valueOf(paidOrders * 100.0 / totalOrders).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal refundRate = paidOrders > 0
                ? BigDecimal.valueOf(refundedOrders * 100.0 / paidOrders).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal cancelRate = totalOrders > 0
                ? BigDecimal.valueOf(cancelledOrders * 100.0 / totalOrders).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal completionRate = paidOrders > 0
                ? BigDecimal.valueOf(completedOrders * 100.0 / paidOrders).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 计算平均支付时长
        List<Order> paidOrderList = orderMapper.selectList(
                Wrappers.lambdaQuery(Order.class)
                        .in(Order::getOrderStatus, OrderStatusEnum.PAID.getCode(), OrderStatusEnum.COMPLETED.getCode())
                        .isNotNull(Order::getPayTime)
        );

        BigDecimal avgPayTime = BigDecimal.ZERO;
        if (!paidOrderList.isEmpty()) {
            long totalMinutes = paidOrderList.stream()
                    .mapToLong(order -> {
                        LocalDateTime createTime = order.getCreateTime();
                        LocalDateTime payTime = order.getPayTime();
                        return java.time.Duration.between(createTime, payTime).toMinutes();
                    })
                    .sum();
            avgPayTime = BigDecimal.valueOf((double) totalMinutes / paidOrderList.size())
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // 计算平均订单金额
        BigDecimal avgOrderAmount = orderMapper.selectList(
                        Wrappers.lambdaQuery(Order.class)
                                .in(Order::getOrderStatus, OrderStatusEnum.PAID.getCode(), OrderStatusEnum.COMPLETED.getCode())
                ).stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(paidOrders, 1)), 2, RoundingMode.HALF_UP);

        return ConversionStatisticsVO.builder()
                .paymentRate(paymentRate)
                .refundRate(refundRate)
                .cancelRate(cancelRate)
                .completionRate(completionRate)
                .avgPayTime(avgPayTime)
                .avgOrderAmount(avgOrderAmount)
                .build();
    }

    @Override
    public UserStatisticsVO getUserStatistics() {
        log.info("获取用户消费统计");

        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        // 查询所有已支付订单
        List<Order> paidOrders = orderMapper.selectList(
                Wrappers.lambdaQuery(Order.class)
                        .in(Order::getOrderStatus, OrderStatusEnum.PAID.getCode(), OrderStatusEnum.COMPLETED.getCode())
        );

        // 按用户分组统计
        Map<Long, List<Order>> userOrderMap = paidOrders.stream()
                .collect(Collectors.groupingBy(Order::getUserId));

        // 计算TOP10用户
        List<UserStatisticsVO.TopUserVO> topUsers = userOrderMap.entrySet().stream()
                .map(entry -> {
                    Long userId = entry.getKey();
                    List<Order> userOrders = entry.getValue();
                    int orderCount = userOrders.size();
                    BigDecimal totalAmount = userOrders.stream()
                            .map(Order::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    String phone = userOrders.get(0).getPhone();
                    // 脱敏处理
                    if (StringUtils.isNotEmpty(phone) && phone.length() == 11) {
                        phone = phone.substring(0, 3) + "****" + phone.substring(7);
                    }

                    return UserStatisticsVO.TopUserVO.builder()
                            .userId(userId)
                            .phone(phone)
                            .orderCount(orderCount)
                            .totalAmount(totalAmount)
                            .build();
                })
                .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()))
                .limit(10)
                .collect(Collectors.toList());

        // 本月新用户数（本月第一次下单的用户）
        Set<Long> monthNewUsers = paidOrders.stream()
                .filter(order -> order.getCreateTime().isAfter(monthStart))
                .map(Order::getUserId)
                .collect(Collectors.toSet());

        // 本月活跃用户数
        Integer activeUsers = Math.toIntExact(paidOrders.stream()
                .filter(order -> order.getCreateTime().isAfter(monthStart))
                .map(Order::getUserId)
                .distinct()
                .count());

        // 平均订单金额
        BigDecimal avgOrderAmount = paidOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(paidOrders.size(), 1)), 2, RoundingMode.HALF_UP);

        return UserStatisticsVO.builder()
                .topUsers(topUsers)
                .newUsers(monthNewUsers.size())
                .activeUsers(activeUsers)
                .avgOrderAmount(avgOrderAmount)
                .build();
    }

    @Override
    public QuickFilterVO getQuickFilter() {
        log.info("获取快捷筛选数据");

        // 待支付订单数（全部）
        Integer pendingOrders = Math.toIntExact(lambdaQuery()
                .eq(Order::getOrderStatus, OrderStatusEnum.PENDING.getCode())
                .count());

        // 已支付订单数（全部，未完成的）
        Integer paidOrders = Math.toIntExact(lambdaQuery()
                .eq(Order::getOrderStatus, OrderStatusEnum.PAID.getCode())
                .count());

        // 已退款订单数（全部）
        Integer refundedOrders = Math.toIntExact(lambdaQuery()
                .eq(Order::getOrderStatus, OrderStatusEnum.REFUNDED.getCode())
                .count());

        // 已取消订单数（全部）
        Integer cancelledOrders = Math.toIntExact(lambdaQuery()
                .eq(Order::getOrderStatus, OrderStatusEnum.CANCELLED.getCode())
                .count());

        // 已完成订单数（全部）
        Integer completedOrders = Math.toIntExact(lambdaQuery()
                .eq(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode())
                .count());

        // 异常订单数（超时未支付）
        Integer abnormalOrders = Math.toIntExact(lambdaQuery()
                .eq(Order::getOrderStatus, OrderStatusEnum.PENDING.getCode())
                .lt(Order::getExpireTime, LocalDateTime.now())
                .count());

        return QuickFilterVO.builder()
                .pendingOrders(pendingOrders)
                .paidOrders(paidOrders)
                .refundedOrders(refundedOrders)
                .cancelledOrders(cancelledOrders)
                .completedOrders(completedOrders)
                .abnormalOrders(abnormalOrders)
                .build();
    }

    @Override
    public List<OrderListVO> getAbnormalOrders() {
        log.info("获取异常订单列表");

        List<Order> abnormalOrders = orderMapper.selectList(
                Wrappers.lambdaQuery(Order.class)
                        .eq(Order::getOrderStatus, OrderStatusEnum.PENDING.getCode())
                        .lt(Order::getExpireTime, LocalDateTime.now())
                        .orderByAsc(Order::getExpireTime)
                        .last("LIMIT 100")
        );

        return abnormalOrders.stream()
                .map(this::convertToOrderListVO)
                .collect(Collectors.toList());
    }

    /**
     * 应用快捷筛选条件
     */
    private void applyQuickFilter(LambdaQueryWrapper<Order> wrapper, String quickFilter) {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime tomorrow = today.plusDays(1);

        switch (quickFilter) {
            case "pending":
                // 待支付订单
                wrapper.eq(Order::getOrderStatus, OrderStatusEnum.PENDING.getCode());
                break;
            case "paid":
                // 已支付订单（未完成的）
                wrapper.eq(Order::getOrderStatus, OrderStatusEnum.PAID.getCode());
                break;
            case "cancelled":
                // 已取消订单
                wrapper.eq(Order::getOrderStatus, OrderStatusEnum.CANCELLED.getCode());
                break;
            case "refunded":
                // 已退款订单
                wrapper.eq(Order::getOrderStatus, OrderStatusEnum.REFUNDED.getCode());
                break;
            case "completed":
                // 已完成订单
                wrapper.eq(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode());
                break;
            case "abnormal":
                // 异常订单（超时未支付）
                wrapper.eq(Order::getOrderStatus, OrderStatusEnum.PENDING.getCode())
                        .lt(Order::getExpireTime, LocalDateTime.now());
                break;
            case "today":
                // 今日订单
                wrapper.between(Order::getCreateTime, today, tomorrow);
                break;
            default:
                break;
        }
    }
}
