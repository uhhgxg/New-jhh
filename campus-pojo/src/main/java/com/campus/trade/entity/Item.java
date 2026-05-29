package com.campus.trade.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item implements Serializable {

    private static final long serialVersionUID = 1L;

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

    //优先级
    private Integer priority;

    //新旧程度: 1全新 2九成新 3八成新 4七成新
    private Integer conditionLevel;

    //原价
    private BigDecimal originalPrice;

    //卖家ID
    private Long sellerId;

    //浏览量
    private Integer viewCount;

    //收藏数
    private Integer favoriteCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createUser;

    private Long updateUser;
}
