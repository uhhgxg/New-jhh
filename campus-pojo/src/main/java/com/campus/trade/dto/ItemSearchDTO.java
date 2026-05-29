package com.campus.trade.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ItemSearchDTO implements Serializable {

    private int page;

    private int pageSize;

    //搜索关键词
    private String keyword;

    //分类ID
    private Long categoryId;

    //最低价格
    private BigDecimal minPrice;

    //最高价格
    private BigDecimal maxPrice;

    //新旧程度
    private Integer conditionLevel;

    //销售状态
    private Integer saleStatus;

    //排序方式: price_asc price_desc time_desc popularity_desc
    private String sortBy;
}
