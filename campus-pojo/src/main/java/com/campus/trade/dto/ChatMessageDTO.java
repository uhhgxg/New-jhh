package com.campus.trade.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChatMessageDTO implements Serializable {

    //发送者ID
    private Long senderId;
    //接收者ID
    private Long receiverId;

    //关联商品ID
    private Long itemId;

    //消息内容
    private String content;

    //消息类型: 1文本 2图片
    private Integer messageType;
}
