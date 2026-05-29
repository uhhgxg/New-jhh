package com.campus.trade.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CategoryDTO implements Serializable {

    //主键
    private Long id;

    //类型 1 商品分类 2 捆绑包分类
    private Integer type;

    //分类名称
    private String name;

    //排序
    private Integer sort;

}
