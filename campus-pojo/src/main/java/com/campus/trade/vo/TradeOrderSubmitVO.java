package com.campus.trade.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易订单提交值对象(VO)
 * 用于封装交易订单提交时的数据传输对象
 * 实现Serializable接口以支持序列化
 *
 * @author CodeGeeX
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeOrderSubmitVO implements Serializable {
    //订单id
    private Long id;
    //交易编号
    private String tradeNo;
    //交易金额
    private BigDecimal totalAmount;
    //交易时间
    private LocalDateTime tradeTime;
}
