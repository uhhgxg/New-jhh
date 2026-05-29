package com.campus.trade.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息值对象类
 * 用于封装聊天相关的数据信息，实现序列化接口以支持网络传输和持久化
 * 使用Lombok注解简化代码，提供getter/setter、构建器模式和构造方法
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVO implements Serializable {

    private Long id;  //消息唯一标识ID

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
