package com.campus.trade.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收藏值对象类(VO)
 * 用于封装收藏相关的数据，实现序列化接口以支持网络传输和持久化
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteVO implements Serializable {

    private Long id;            // 收藏记录的唯一标识ID

    //商品ID
    private Long itemId;        // 被收藏商品的唯一标识ID

    //商品名称
    private String itemName;    // 被收藏商品的名称

    //商品图片
    private String images;      // 被收藏商品的图片URL，可能包含多个图片路径

    //商品价格
    private BigDecimal unitPrice; // 被收藏商品的单价，使用BigDecimal确保精度

    //销售状态
    private Integer saleStatus;  // 商品的销售状态，如0-下架，1-在售等

    //收藏时间
    private LocalDateTime createTime; // 用户收藏该商品的时间点
}
