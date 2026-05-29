package com.campus.trade.dto;

import com.campus.trade.entity.OrderDetail;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TradeOrderDTO implements Serializable {

    private Long id;

    //交易编号
    private String tradeNo;

    //交易状态 1待付款 2待发货 3已发货 4已完成 5已取消 6退款中 7已退款
    private Integer tradeStatus;

    //买家ID
    private Long buyerId;

    //卖家ID
    private Long sellerId;

    //商品ID
    private Long itemId;

    //收货地址ID
    private Long deliveryAddressId;

    //数量
    private Integer quantity;

    //交易时间
    private LocalDateTime tradeTime;

    //支付时间
    private LocalDateTime paymentTime;

    //支付方式 1微信 2支付宝
    private Integer paymentMethod;

    //总金额
    private BigDecimal totalAmount;

    //运费
    private BigDecimal shippingFee;

    //交易备注
    private String tradeRemark;

    //快递单号
    private String trackingNumber;

    //确认收货时间
    private LocalDateTime confirmTime;

    //用户名
    private String userName;

    //手机号
    private String phone;

    //地址
    private String address;

    //收货人
    private String consignee;

    private List<OrderDetail> orderDetails;
}
