package com.campus.trade.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价值对象(VO)类，用于封装评价相关的数据
 * 使用了Lombok注解简化代码，包括@Data(生成getter/setter等方法)、
 * @Builder(构建器模式)、@NoArgsConstructor(无参构造)、
 * @AllArgsConstructor(全参构造)
 * 实现了Serializable接口，支持序列化
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationVO implements Serializable {

    private Long id; // 评价ID

    //订单ID
    private Long orderId;

    //评价人ID
    private Long reviewerId;

    //评价人名称
    private String reviewerName;

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
