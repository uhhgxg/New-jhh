package com.campus.trade.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

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

    //发送时间
    private LocalDateTime sendTime;

    //是否已读
    private Integer isRead;
}
