package com.campus.trade.controller.user;

import com.campus.trade.context.BaseContext;
import com.campus.trade.dto.ChatMessageDTO;
import com.campus.trade.result.Result;
import com.campus.trade.service.ChatService;
import com.campus.trade.vo.ChatMessageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天消息控制器
 * 提供发送消息、获取聊天记录、标记消息已读等功能
 */
@RestController
@RequestMapping("/user/chat")
@Slf4j
@Api(tags = "聊天消息")
public class ChatController {

    @Autowired
    private ChatService chatService; // 注入聊天服务

    /**
     * 发送消息接口
     * @param chatMessageDTO 聊天消息数据传输对象
     * @return 返回发送后的消息视图对象
     */
    @PostMapping("/send")
    @ApiOperation("发送消息")
    public Result<ChatMessageVO> send(@RequestBody ChatMessageDTO chatMessageDTO) {
        log.info("发送消息：{}", chatMessageDTO);
        chatMessageDTO.setSenderId(BaseContext.getCurrentId()); // 设置发送者ID
        ChatMessageVO chatMessageVO = chatService.sendMessage(chatMessageDTO);
        return Result.success(chatMessageVO);
    }

    /**
     * 获取与指定用户的聊天记录
     * @param receiverId 接收者ID
     * @return 返回聊天记录列表
     */
    @GetMapping("/conversation/{receiverId}")
    @ApiOperation("获取与对方的聊天记录")
    public Result<List<ChatMessageVO>> getConversation(@PathVariable Long receiverId) {
        Long senderId = BaseContext.getCurrentId();
        log.info("获取聊天记录，senderId：{}，receiverId：{}", senderId, receiverId);
        List<ChatMessageVO> list = chatService.getConversation(senderId, receiverId);
        return Result.success(list);
    }

    /**
     * 获取关于某个商品的聊天记录
     * @param itemId 商品ID
     * @param receiverId 接收者ID
     * @return 返回关于该商品的聊天记录列表
     */
    @GetMapping("/item/{itemId}/{receiverId}")
    @ApiOperation("获取关于某个商品的聊天记录")
    public Result<List<ChatMessageVO>> getItemMessages(@PathVariable Long itemId, @PathVariable Long receiverId) {
        Long senderId = BaseContext.getCurrentId();
        log.info("获取商品聊天记录，itemId：{}，senderId：{}，receiverId：{}", itemId, senderId, receiverId);
        List<ChatMessageVO> list = chatService.getMessagesByItem(itemId, senderId, receiverId);
        return Result.success(list);
    }

    /**
     * 标记来自指定发送者的消息为已读
     * @param senderId 发送者ID
     * @return 返回操作结果
     */
    @PutMapping("/read/{senderId}")
    @ApiOperation("标记消息为已读")
    public Result markAsRead(@PathVariable Long senderId) {
        Long receiverId = BaseContext.getCurrentId();
        log.info("标记消息为已读，senderId：{}，receiverId：{}", senderId, receiverId);
        chatService.markAsRead(senderId, receiverId);
        return Result.success();
    }
}
