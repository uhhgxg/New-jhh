package com.campus.trade.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 数据概览
 * 用于展示业务数据的视图对象(Value Object)，包含营业额、订单数、订单完成率等关键业务指标
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDataVO implements Serializable {

    private Double turnover;//营业额：统计周期内的总营业额

    private Integer validOrderCount;//有效订单数：统计周期内已完成的订单数量

    private Double orderCompletionRate;//订单完成率：已完成订单占总订单的百分比

    private Double unitPrice;//平均客单价：平均每笔订单的金额

    private Integer newUsers;//新增用户数：统计周期内新注册的用户数量

}
