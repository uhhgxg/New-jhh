package com.campus.trade.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 交易订单概览数据
 * 该类用于封装交易订单的各种状态统计数据，提供了订单状态的概览信息
 * 使用了Lombok注解简化代码，包括@Data（getter/setter）、@Builder（构建器模式）、
 * @NoArgsConstructor（无参构造）和@AllArgsConstructor（全参构造）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeOrderOverViewVO implements Serializable {
    //待付款数量
    //表示用户尚未完成支付的订单数量
    private Integer pendingPaymentOrders;

    //待发货数量
    //表示卖家已接单但尚未发货的订单数量
    private Integer pendingShipmentOrders;

    //已发货数量
    //表示卖家已发货但买家尚未确认收货的订单数量
    private Integer shippedOrders;

    //已完成数量
    //表示用户已确认收货或系统自动确认的订单数量
    private Integer completedOrders;

    //已取消数量
    //表示用户或商家主动取消的订单数量
    private Integer cancelledOrders;

    //全部订单
    //系统中所有订单的总数量
    private Integer allOrders;
}
