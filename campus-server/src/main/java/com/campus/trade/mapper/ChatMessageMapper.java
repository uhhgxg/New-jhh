package com.campus.trade.mapper;

import com.campus.trade.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    void insert(ChatMessage chatMessage);

    List<ChatMessage> getBySenderAndReceiver(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    List<ChatMessage> getByItemId(@Param("itemId") Long itemId, @Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    void markAsRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
}
