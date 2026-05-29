package com.campus.trade.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品值对象(VO)类，用于封装商品相关的数据
 * 使用了Lombok注解简化代码
 * 实现了Serializable接口以支持序列化
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemVO implements Serializable {

    private Long id;                    //商品ID
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
    //新旧程度
    private Integer conditionLevel;
    //原价
    private BigDecimal originalPrice;
    //卖家ID
    private Long sellerId;
    //浏览量
    private Integer viewCount;
    //收藏数
    private Integer favoriteCount;
    //更新时间
    private LocalDateTime updateTime;
    //分类名称
    private String categoryName;
}
