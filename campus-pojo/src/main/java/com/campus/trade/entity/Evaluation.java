package com.campus.trade.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evaluation implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //订单ID
    private Long orderId;

    //评价人ID
    private Long reviewerId;

    //被评价人ID
    private Long revieweeId;

    //评分: 1-5星
    private Integer rating;

    //评价内容
    private String content;

    //评价时间
    private LocalDateTime createTime;

    //评价类型: 1对卖家 2对买家
    private Integer type;
}
