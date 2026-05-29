package com.campus.trade.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * BundleItemVO类是一个用于封装商品信息的值对象(Value Object)
 * 实现了Serializable接口，以便可以进行序列化操作
 * 使用@Data注解来自动生成getter、setter等方法
 */
@Data
public class BundleItemVO implements Serializable {

    //商品名称，用于存储商品的名称信息
    private String name;

    //份数，用于存储商品的数量或份数信息
    private Integer copies;

    //商品图片，用于存储商品图片的URL或路径信息
    private String image;

    //商品描述，用于存储商品的详细描述信息
    private String description;

    //状态，用于存储商品的状态信息（如上架、下架等）
    private Integer status;
}
