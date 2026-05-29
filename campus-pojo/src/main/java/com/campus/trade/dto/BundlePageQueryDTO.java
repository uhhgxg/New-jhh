package com.campus.trade.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * BundlePageQueryDTO类用于分页查询Bundle的参数封装
 * 实现Serializable接口以支持序列化操作
 */
@Data  // 使用Lombok的@Data注解自动生成getter、setter等方法
public class BundlePageQueryDTO implements Serializable {

    private int page;  // 当前页码

    private int pageSize;  // 每页显示条数

    private String name;  // Bundle名称，用于模糊查询

    //分类id
    private Integer categoryId;  // Bundle分类ID，用于按分类筛选

    //状态 0表示禁用 1表示启用
    private Integer status;  // Bundle状态，0表示禁用，1表示启用
}
