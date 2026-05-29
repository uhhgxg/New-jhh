package com.campus.trade.mapper;

import com.campus.trade.entity.ChatHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI聊天历史记录数据访问层
 */
@Mapper
public interface ChatHistoryMapper {

    /**
     * 创建聊天历史表（如果不存在）
     */
    void createTableIfNotExists();

    /**
     * 为已有表补充 user_id 列（向前兼容）
     */
    void addUserIdColumnIfMissing();

    /**
     * 插入一条聊天记录
     */
    void insert(ChatHistory chatHistory);

    /**
     * 批量插入聊天记录
     */
    void batchInsert(@Param("list") List<ChatHistory> list);

    /**
     * 根据会话ID查询聊天记录（按时间升序，限制条数）
     */
    List<ChatHistory> getByConversationId(@Param("conversationId") String conversationId,
                                          @Param("limit") int limit);

    /**
     * 根据会话ID删除所有聊天记录
     */
    void deleteByConversationId(@Param("conversationId") String conversationId);

    /**
     * 查询用户在指定会话中是否存在消息，存在则回填 userId
     */
    void fillUserId(@Param("conversationId") String conversationId, @Param("userId") Long userId);

    /**
     * 查询指定用户的所有会话列表（每个会话取首条消息作为摘要）
     */
    List<ChatHistory> getConversationsByUserId(@Param("userId") Long userId);
}
