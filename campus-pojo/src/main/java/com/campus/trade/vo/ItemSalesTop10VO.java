package com.campus.trade.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品销售前十数据值对象(Value Object)
 * 用于封装商品名称列表和对应的销量列表
 * 实现了Serializable接口以支持序列化
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemSalesTop10VO implements Serializable {

    //商品名称列表，以逗号分隔
    private String nameList;

    //销量列表，以逗号分隔
    private String numberList;
}
