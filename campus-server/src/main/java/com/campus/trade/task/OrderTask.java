package com.campus.trade.task;

import com.campus.trade.entity.TradeOrder;
import com.campus.trade.mapper.TradeOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private TradeOrderMapper tradeOrderMapper;

    /**
     * 每分钟检查一次支付超时订单
     * 下单后超过30分钟仍未支付则自动取消（二手交易场景给予更长决策时间）
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void processTimeoutOrder() {
        log.info("=== 定时任务：处理支付超时订单 ===");

        LocalDateTime deadline = LocalDateTime.now().minusMinutes(30);
        List<TradeOrder> orders = tradeOrderMapper.getByStatusAndTradeTimeLT(TradeOrder.PENDING_PAYMENT, deadline);

        if (orders == null || orders.isEmpty()) {
            return;
        }

        for (TradeOrder order : orders) {
            order.setTradeStatus(TradeOrder.CANCELLED);
            order.setCancelReason("支付超时，系统自动取消");
            order.setCancelTime(LocalDateTime.now());
            tradeOrderMapper.update(order);
            log.info("已自动取消订单：{}", order.getTradeNo());
        }
    }

    /**
     * 每天凌晨1点检查并自动完成已发货超过7天的订单
     * 二手交易场景下，买家需要时间验货，因此留足7天确认期后才自动完成
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processShippedOrder() {
        log.info("=== 定时任务：自动完成已发货超过7天的订单 ===");

        LocalDateTime deadline = LocalDateTime.now().minusDays(7);
        List<TradeOrder> orders = tradeOrderMapper.getByStatusAndPaymentTimeLT(TradeOrder.SHIPPED, deadline);

        if (orders == null || orders.isEmpty()) {
            return;
        }

        for (TradeOrder order : orders) {
            order.setTradeStatus(TradeOrder.COMPLETED);
            tradeOrderMapper.update(order);
            log.info("已自动完成订单：{}（支付时间：{}）", order.getTradeNo(), order.getPaymentTime());
        }
    }
}
