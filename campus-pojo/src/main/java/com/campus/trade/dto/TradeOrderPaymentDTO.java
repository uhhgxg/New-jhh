package com.campus.trade.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class TradeOrderPaymentDTO implements Serializable {
    //交易编号
    private String tradeNo;

    //支付方式
    private Integer paymentMethod;
}
