package com.campus.trade.service;

import com.campus.trade.dto.*;
import com.campus.trade.result.PageResult;
import com.campus.trade.vo.*;

public interface TradeOrderService {

    TradeOrderSubmitVO submitOrder(TradeOrderSubmitDTO tradeOrderSubmitDTO);

    TradeOrderPaymentVO payment(TradeOrderPaymentDTO tradeOrderPaymentDTO) throws Exception;

    void paySuccess(String tradeNo);

    PageResult pageQuery(TradeOrderPageQueryDTO tradeOrderPageQueryDTO);

    TradeOrderVO getOrderDetail(Long id);

    void cancelOrder(Long id);

    PageResult conditionSearch(TradeOrderPageQueryDTO tradeOrderPageQueryDTO);

    TradeOrderStatisticsVO statistics();

    void confirm(TradeOrderConfirmDTO tradeOrderConfirmDTO);

    void rejection(TradeOrderRejectionDTO tradeOrderRejectionDTO);

    void cancel(TradeOrderCancelDTO tradeOrderCancelDTO);

    void ship(Long id, String trackingNumber);

    void complete(Long id);

    void reminder(Long id);

    PageResult soldOrders(TradeOrderPageQueryDTO tradeOrderPageQueryDTO);
}
