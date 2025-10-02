package com.snzh.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snzh.domain.entity.OrderItem;
import com.snzh.domain.vo.OrderItemVO;
import com.snzh.enums.ItemTypeEnum;
import com.snzh.mapper.OrderItemMapper;
import com.snzh.service.IOrderItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author haibara
 * @description 订单明细服务实现类
 * @since 2025/10/2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements IOrderItemService {

    private final OrderItemMapper orderItemMapper;

    @Override
    public List<OrderItemVO> getByOrderId(Long orderId) {
        List<OrderItem> orderItems = orderItemMapper.selectList(
                Wrappers.lambdaQuery(OrderItem.class)
                        .eq(OrderItem::getOrderId, orderId)
                        .orderByAsc(OrderItem::getId)
        );

        return orderItems.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchCreate(List<OrderItem> orderItems) {
        return saveBatch(orderItems);
    }

    /**
     * 转换为VO
     */
    private OrderItemVO convertToVO(OrderItem orderItem) {
        OrderItemVO vo = BeanUtil.copyProperties(orderItem, OrderItemVO.class);
        vo.setItemTypeDesc(ItemTypeEnum.getMsg(orderItem.getItemType()));
        return vo;
    }
}
