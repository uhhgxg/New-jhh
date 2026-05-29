package com.campus.trade.vo;

import com.campus.trade.entity.BundleItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * BundleVO类是一个表示捆绑包的值对象(Value Object)，实现了Serializable接口以支持序列化操作。
 * 使用了Lombok注解来自动生成getter、setter、builder模式以及构造方法。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BundleVO implements Serializable {

    // 捆绑包的唯一标识符
    private Long id;

    //分类id，用于标识捆绑包所属的商品分类
    private Long categoryId;

    //捆绑包名称
    private String name;

    //捆绑包价格
    private BigDecimal price;

    //状态 0:停用 1:启用
    private Integer status;

    //描述信息
    private String description;

    //图片
    private String image;

    //更新时间
    private LocalDateTime updateTime;

    //分类名称
    private String categoryName;

    //捆绑包和商品的关联关系
    private List<BundleItem> bundleItems = new ArrayList<>();
}
