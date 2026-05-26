package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI聊天历史记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 会话ID */
    private String conversationId;

    /** 用户ID */
    private Long userId;

    /** 消息类型：USER / ASSISTANT / SYSTEM */
    private String messageType;

    /** 消息内容 */
    private String content;

    /** 创建时间 */
    private LocalDateTime createTime;
}
