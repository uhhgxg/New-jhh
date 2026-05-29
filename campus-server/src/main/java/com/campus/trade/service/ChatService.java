package com.campus.trade.service;

import com.campus.trade.dto.ChatMessageDTO;
import com.campus.trade.vo.ChatMessageVO;

import java.util.List;

public interface ChatService {
    ChatMessageVO sendMessage(ChatMessageDTO chatMessageDTO);

    List<ChatMessageVO> getConversation(Long senderId, Long receiverId);

    List<ChatMessageVO> getMessagesByItem(Long itemId, Long senderId, Long receiverId);

    void markAsRead(Long senderId, Long receiverId);
}
