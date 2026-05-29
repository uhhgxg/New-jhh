package com.campus.trade.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 交易订单统计值对象
 * 用于封装交易订单相关的统计数据，包括待发货、已发货和退款中的数量
 * 实现了Serializable接口，支持序列化操作
 */
@Data
public class TradeOrderStatisticsVO implements Serializable {
    //待发货数量
    private Integer pendingShipment;

    //已发货数量
    private Integer shipped;

    //退款中数量
    private Integer refunding;
}
