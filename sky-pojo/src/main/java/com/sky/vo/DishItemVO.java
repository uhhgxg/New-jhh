package com.sky.vo;

import lombok.Data;
import java.io.Serializable;

@Data
public class DishItemVO implements Serializable {

    //菜品名称
    private String name;

    //份数
    private Integer copies;

    //菜品图片
    private String image;

    //菜品描述
    private String description;

    // 新增菜品状态字段，用于启售校验
    private Integer status; 

}
