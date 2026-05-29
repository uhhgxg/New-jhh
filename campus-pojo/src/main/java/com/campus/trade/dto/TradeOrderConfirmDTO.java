package com.campus.trade.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TradeOrderConfirmDTO implements Serializable {

    private Long id;
    //交易状态
    private Integer tradeStatus;
}
