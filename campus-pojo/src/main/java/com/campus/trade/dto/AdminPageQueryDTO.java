package com.campus.trade.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员页面查询数据传输对象
 * 该类用于封装管理员查询条件，包括姓名、页码和每页显示记录数
 * 实现Serializable接口以支持序列化操作
 */
@Data
public class AdminPageQueryDTO implements Serializable {

    //管理员姓名，用于按姓名条件查询
    private String name;

    //页码
    private int page;

    //每页显示记录数
    private int pageSize;
}
