package com.campus.trade.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PasswordEditDTO implements Serializable {

    //管理员id
    private Long adminId;

    //旧密码
    private String oldPassword;

    //新密码
    private String newPassword;
}
