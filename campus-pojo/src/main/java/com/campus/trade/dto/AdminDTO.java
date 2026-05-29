package com.campus.trade.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员数据传输对象(AdminDTO)
 * 用于在系统各层之间传递管理员相关信息
 * 实现Serializable接口以支持序列化操作
 */
@Data  // 使用Lombok的@Data注解自动生成getter、setter等方法
public class AdminDTO implements Serializable {

    private Long id;        // 管理员ID，唯一标识符

    private String username; // 管理员登录用户名

    private String name;    // 管理员真实姓名

    private String phone;   // 管理员联系电话

    private String sex;     // 管理员性别

    private String idNumber; // 管理员身份证号码
}
