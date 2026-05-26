package com.sky.repository;


import com.sky.context.BaseContext;

import com.sky.entity.ChatHistory;
import com.sky.mapper.ChatHistoryMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 基于 MySQL 的聊天历史持久化仓库
 * <p>
 * 实现 {@link ChatMemory} 接口，替代默认的内存存储，使会话历史在应用重启后仍可恢复。
 * 同时提供面向用户的历史记录查询方法。
 * </p>
 */
@Repository
@Slf4j
public class ChatHistoryRepository implements ChatMemory {

    private static final int MAX_MESSAGES = 50;

    @Autowired
    private ChatHistoryMapper chatHistoryMapper;

    @PostConstruct
    public void init() {
        try {
            chatHistoryMapper.createTableIfNotExists();
            log.info("聊天历史表初始化完成");
        } catch (Exception e) {
            log.warn("聊天历史表初始化失败，可能已存在: {}", e.getMessage());
        }
        try {
            chatHistoryMapper.addUserIdColumnIfMissing();
            log.info("user_id 列迁移完成");
        } catch (Exception e) {
            log.debug("user_id 列已存在或迁移失败: {}", e.getMessage());
        }
    }

    // ==================== ChatMemory 接口实现 ====================

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        Long userId = BaseContext.getCurrentId();
        List<ChatHistory> entities = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Message message : messages) {
            String type = message.getMessageType().getValue();
            String content = message.getText();

            if (content == null || content.trim().isEmpty()
                    || MessageType.SYSTEM.getValue().equals(type)) {
                continue;
            }

            entities.add(ChatHistory.builder()
                    .conversationId(conversationId)
                    .userId(userId)
                    .messageType(type)
                    .content(content)
                    .createTime(now)
                    .build());
        }

        if (!entities.isEmpty()) {
            chatHistoryMapper.batchInsert(entities);
            log.debug("持久化 {} 条聊天记录, conversationId={}, userId={}",
                    entities.size(), conversationId, userId);
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        List<ChatHistory> records = chatHistoryMapper.getByConversationId(conversationId, MAX_MESSAGES);
        List<Message> messages = new ArrayList<>();

        for (ChatHistory record : records) {
            if (MessageType.USER.getValue().equals(record.getMessageType())) {
                messages.add(new UserMessage(record.getContent()));
            } else if (MessageType.ASSISTANT.getValue().equals(record.getMessageType())) {
                messages.add(new AssistantMessage(record.getContent()));
            }
        }

        log.debug("加载 {} 条历史消息, conversationId={}", messages.size(), conversationId);
        return messages;
    }

    @Override
    public void clear(String conversationId) {
        chatHistoryMapper.deleteByConversationId(conversationId);
        log.info("清除聊天历史, conversationId={}", conversationId);
    }

    // ==================== 用户视角的查询方法 ====================

    /**
     * 查询当前用户的所有会话列表，每个会话返回首条用户消息作为摘要
     */
    public List<ChatHistory> getMyConversations() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Collections.emptyList();
        }
        return chatHistoryMapper.getConversationsByUserId(userId);
    }

    /**
     * 查询指定会话的完整消息列表（用于历史详情查看）
     */
    public List<ChatHistory> getConversationMessages(String conversationId) {
        return chatHistoryMapper.getByConversationId(conversationId, MAX_MESSAGES);
    }

    /**
     * 删除指定会话
     */
    public void deleteConversation(String conversationId) {
        chatHistoryMapper.deleteByConversationId(conversationId);
        log.info("删除会话, conversationId={}", conversationId);
    }
}
