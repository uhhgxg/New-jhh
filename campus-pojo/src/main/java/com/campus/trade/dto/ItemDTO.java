package com.campus.trade.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ItemDTO implements Serializable {

    private Long id;
    //商品名称
    private String itemName;
    //分类ID
    private Long categoryId;
    //单价
    private BigDecimal unitPrice;
    //图片（多图，逗号分隔）
    private String images;
    //商品描述
    private String itemDescription;
    //销售状态: 0下架 1上架
    private Integer saleStatus;
    //新旧程度: 1全新 2九成新 3八成新 4七成新
    private Integer conditionLevel;
    //原价
    private BigDecimal originalPrice;
    //卖家ID
    private Long sellerId;
}
