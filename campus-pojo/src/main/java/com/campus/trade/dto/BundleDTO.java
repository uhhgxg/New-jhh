package com.campus.trade.dto;

import com.campus.trade.entity.BundleItem;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class BundleDTO implements Serializable {

    private Long id;

    //分类id
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

    //捆绑包商品关系
    private List<BundleItem> bundleItems = new ArrayList<>();
}
