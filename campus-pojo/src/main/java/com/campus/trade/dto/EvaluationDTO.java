package com.campus.trade.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EvaluationDTO implements Serializable {

    //订单ID
    private Long orderId;

    //被评价人ID
    private Long revieweeId;

    //评分: 1-5星
    private Integer rating;

    //评价内容
    private String content;

    //评价类型: 1对卖家 2对买家
    private Integer type;
}
