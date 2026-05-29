package com.campus.trade.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class TradeOrderSubmitDTO implements Serializable {
    //收货地址ID
    private Long deliveryAddressId;
    //商品ID
    private Long itemId;
    //数量
    private Integer quantity;
    //支付方式
    private Integer paymentMethod;
    //交易备注
    private String tradeRemark;
    //总金额
    private BigDecimal totalAmount;
    //运费
    private BigDecimal shippingFee;
}
