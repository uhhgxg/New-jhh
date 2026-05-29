package com.campus.trade.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品详情值对象（VO）类
 * 用于封装和传输商品详细信息的数据结构
 * 实现了Serializable接口，支持序列化操作
 * 使用了Lombok注解简化代码：
 * @Data - 自动生成getter、setter、toString等方法
 * @Builder - 提供构建器模式支持
 * @NoArgsConstructor - 生成无参构造方法
 * @AllArgsConstructor - 生成全参构造方法
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDetailVO implements Serializable {

    private Long id;                    // 商品ID
    //商品名称
    private String itemName;            // 商品的完整名称
    //分类ID
    private Long categoryId;            // 商品所属分类的唯一标识
    //分类名称
    private String categoryName;        // 商品所属分类的名称
    //单价
    private BigDecimal unitPrice;        // 商品当前的销售单价
    //原价
    private BigDecimal originalPrice;    // 商品原始定价
    //图片（多图，逗号分隔）
    private String images;               // 商品图片URL，多个URL用逗号分隔
    //商品描述
    private String itemDescription;      // 商品的详细描述信息
    //新旧程度
    private Integer conditionLevel;     // 商品的新旧程度等级
    //销售状态
    private Integer saleStatus;          // 商品的当前销售状态
    //卖家ID
    private Long sellerId;               // 卖家的唯一标识
    //卖家名称
    private String sellerName;           // 卖家的名称或店铺名
    //浏览量
    private Integer viewCount;           // 商品的浏览次数统计
    //收藏数
    private Integer favoriteCount;       // 商品的收藏次数统计
    //更新时间
    private LocalDateTime updateTime;    // 商品信息的最后更新时间
}
