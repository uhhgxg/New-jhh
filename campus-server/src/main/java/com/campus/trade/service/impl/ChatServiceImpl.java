package com.campus.trade.service.impl;

import com.campus.trade.context.BaseContext;
import com.campus.trade.dto.ChatMessageDTO;
import com.campus.trade.entity.ChatMessage;
import com.campus.trade.mapper.ChatMessageMapper;
import com.campus.trade.service.ChatService;
import com.campus.trade.vo.ChatMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    /**
     * 发送消息
     */
    @Override
    @Transactional
    public ChatMessageVO sendMessage(ChatMessageDTO chatMessageDTO) {
        log.info("发送消息，receiverId：{}，itemId：{}", chatMessageDTO.getReceiverId(), chatMessageDTO.getItemId());

        Long senderId = BaseContext.getCurrentId();

        ChatMessage chatMessage = ChatMessage.builder()
                .senderId(senderId)
                .receiverId(chatMessageDTO.getReceiverId())
                .itemId(chatMessageDTO.getItemId())
                .content(chatMessageDTO.getContent())
                .messageType(chatMessageDTO.getMessageType())
                .sendTime(LocalDateTime.now())
                .isRead(0)
                .build();

        chatMessageMapper.insert(chatMessage);
        log.info("消息发送成功，id：{}", chatMessage.getId());

        ChatMessageVO chatMessageVO = new ChatMessageVO();
        BeanUtils.copyProperties(chatMessage, chatMessageVO);
        return chatMessageVO;
    }

    /**
     * 获取与某用户的对话记录
     */
    @Override
    public List<ChatMessageVO> getConversation(Long senderId, Long receiverId) {
        log.info("获取对话记录，senderId：{}，receiverId：{}", senderId, receiverId);

        List<ChatMessage> messages = chatMessageMapper.getBySenderAndReceiver(senderId, receiverId);
        List<ChatMessageVO> voList = new ArrayList<>();

        if (messages != null) {
            for (ChatMessage message : messages) {
                ChatMessageVO vo = new ChatMessageVO();
                BeanUtils.copyProperties(message, vo);
                voList.add(vo);
            }
        }

        return voList;
    }

    /**
     * 根据商品ID获取消息
     */
    @Override
    public List<ChatMessageVO> getMessagesByItem(Long itemId, Long senderId, Long receiverId) {
        log.info("根据商品获取消息，itemId：{}，senderId：{}，receiverId：{}", itemId, senderId, receiverId);

        List<ChatMessage> messages = chatMessageMapper.getByItemId(itemId, senderId, receiverId);
        List<ChatMessageVO> voList = new ArrayList<>();

        if (messages != null) {
            for (ChatMessage message : messages) {
                ChatMessageVO vo = new ChatMessageVO();
                BeanUtils.copyProperties(message, vo);
                voList.add(vo);
            }
        }

        return voList;
    }

    /**
     * 标记消息为已读
     */
    @Override
    public void markAsRead(Long senderId, Long receiverId) {
        log.info("标记消息为已读，senderId：{}，receiverId：{}", senderId, receiverId);
        chatMessageMapper.markAsRead(senderId, receiverId);
    }
}
