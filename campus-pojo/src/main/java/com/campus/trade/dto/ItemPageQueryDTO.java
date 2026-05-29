package com.campus.trade.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ItemPageQueryDTO implements Serializable {

    private int page;

    private int pageSize;

    private String itemName;

    //分类id
    private Long categoryId;

    //销售状态
    private Integer saleStatus;

    //新旧程度
    private Integer conditionLevel;

    //最低价格
    private BigDecimal minPrice;

    //最高价格
    private BigDecimal maxPrice;
}
