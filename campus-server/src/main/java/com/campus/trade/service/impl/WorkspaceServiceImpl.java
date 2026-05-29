package com.campus.trade.service.impl;

import com.campus.trade.constant.StatusConstant;
import com.campus.trade.entity.TradeOrder;
import com.campus.trade.mapper.ItemMapper;
import com.campus.trade.mapper.TradeOrderMapper;
import com.campus.trade.mapper.BundleMapper;
import com.campus.trade.mapper.UserMapper;
import com.campus.trade.service.WorkspaceService;
import com.campus.trade.vo.BusinessDataVO;
import com.campus.trade.vo.ItemOverViewVO;
import com.campus.trade.vo.TradeOrderOverViewVO;
import com.campus.trade.vo.BundleOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private TradeOrderMapper tradeOrderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private BundleMapper bundleMapper;

    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        Map map = new HashMap();
        map.put("beginTime", begin);
        map.put("endTime", end);

        Integer totalOrderCount = tradeOrderMapper.countByMap(map);

        map.put("tradeStatus", TradeOrder.COMPLETED);
        BigDecimal turnoverDecimal = tradeOrderMapper.sumByMap(map);
        Double turnover = turnoverDecimal == null ? 0.0 : turnoverDecimal.doubleValue();

        Integer validOrderCount = tradeOrderMapper.countByMap(map);

        Double unitPrice = 0.0;
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
            if (validOrderCount != 0) {
                unitPrice = turnover / validOrderCount;
            }
        }

        Integer newUsers = userMapper.countByMap(map);

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }

    public TradeOrderOverViewVO getOrderOverView() {
        Map map = new HashMap();
        map.put("beginTime", LocalDateTime.now().with(LocalTime.MIN));

        map.put("tradeStatus", TradeOrder.PENDING_PAYMENT);
        Integer pendingPaymentOrders = tradeOrderMapper.countByMap(map);

        map.put("tradeStatus", TradeOrder.PENDING_SHIPMENT);
        Integer pendingShipmentOrders = tradeOrderMapper.countByMap(map);

        map.put("tradeStatus", TradeOrder.SHIPPED);
        Integer shippedOrders = tradeOrderMapper.countByMap(map);

        map.put("tradeStatus", TradeOrder.COMPLETED);
        Integer completedOrders = tradeOrderMapper.countByMap(map);

        map.put("tradeStatus", TradeOrder.CANCELLED);
        Integer cancelledOrders = tradeOrderMapper.countByMap(map);

        map.put("tradeStatus", null);
        Integer allOrders = tradeOrderMapper.countByMap(map);

        return TradeOrderOverViewVO.builder()
                .pendingPaymentOrders(pendingPaymentOrders)
                .pendingShipmentOrders(pendingShipmentOrders)
                .shippedOrders(shippedOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    public ItemOverViewVO getItemOverView() {
        Map map = new HashMap();
        map.put("saleStatus", StatusConstant.ON_SALE);
        Integer sold = itemMapper.countByMap(map);

        map.put("saleStatus", StatusConstant.OFF_SALE);
        Integer discontinued = itemMapper.countByMap(map);

        return ItemOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    public BundleOverViewVO getBundleOverView() {
        Map map = new HashMap();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = bundleMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = bundleMapper.countByMap(map);

        return BundleOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}
