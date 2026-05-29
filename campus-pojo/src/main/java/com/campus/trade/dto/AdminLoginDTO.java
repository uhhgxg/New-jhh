package com.campus.trade.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 管理员登录数据传输对象(DTO)
 * 用于封装管理员登录时需要传递的数据
 * 实现Serializable接口以支持序列化
 */
@Data
@ApiModel(description = "管理员登录时传递的数据模型")
public class AdminLoginDTO implements Serializable {

    @ApiModelProperty("用户名")
    private String username;  // 管理员登录用户名

    @ApiModelProperty("密码")
    private String password;  // 管理员登录密码
}
