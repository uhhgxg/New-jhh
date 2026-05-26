package com.sky.controller.user;

import com.sky.constant.MessageConstant;
import com.sky.dto.AiChatDTO;
import com.sky.entity.ChatHistory;
import com.sky.repository.ChatHistoryRepository;
import com.sky.result.Result;
import com.sky.service.AiAssistantService;
import com.sky.vo.AiChatVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;



/**
 * C端-AI助手控制器
 * 提供AI助手聊天功能，包括普通聊天和流式聊天
 * 同时提供历史会话的查看、删除等功能
 */
@RestController("userAiAssistantController")  // 使用特定名称的Bean，避免与其他Controller冲突
@RequestMapping("/user/ai")  // 设置基础请求路径
@Slf4j  // 添加日志支持
@Api(tags = "C端-AI助手接口")  // Swagger API文档注解
public class AiAssistantController {

    @Autowired
    private AiAssistantService aiAssistantService;  // AI助手服务，处理聊天相关逻辑

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;  // 聊天历史数据访问层

    /**
     * AI助手普通聊天接口
     * @param aiChatDTO 聊天数据传输对象，包含用户发送的消息
     * @return 返回AI助手的回复结果
     */
    @PostMapping("/chat")
    @ApiOperation("AI助手聊天（普通）")
    public Result<AiChatVO> chat(@RequestBody AiChatDTO aiChatDTO) {
        log.info("AI助手接收消息: {}", aiChatDTO.getMessage());  // 记录接收到的消息

        // 检查消息是否为空
        if (aiChatDTO.getMessage() == null || aiChatDTO.getMessage().trim().isEmpty()) {
            return Result.error(MessageConstant.AI_EMPTY_MESSAGE);
        }

        // 调用服务层处理聊天请求
        AiChatVO vo = aiAssistantService.chat(aiChatDTO);
        return Result.success(vo);
    }

    /**
     * AI助手流式聊天接口（SSE）
     * 使用Server-Sent Events实现流式响应
     * @param aiChatDTO 聊天数据传输对象，包含用户发送的消息
     * @return 返回流式响应，包含AI助手的逐步回复
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ApiOperation("AI助手聊天（流式 SSE）")
    public Flux<String> chatStream(@RequestBody AiChatDTO aiChatDTO) {
        log.info("AI助手流式接收消息: {}", aiChatDTO.getMessage());  // 记录接收到的消息

        // 检查消息是否为空
        if (aiChatDTO.getMessage() == null || aiChatDTO.getMessage().trim().isEmpty()) {
            return Flux.just(MessageConstant.AI_EMPTY_MESSAGE);
        }

        // 调用服务层处理流式聊天请求
        return aiAssistantService.chatStream(aiChatDTO);
    }

    /**
     * 获取用户的历史会话列表
     * @return 返回用户的所有历史会话记录
     */
    @GetMapping("/history/conversations")
    @ApiOperation("查看历史会话列表")
    public Result<List<ChatHistory>> listConversations() {
        // 从数据库获取用户的所有会话
        List<ChatHistory> conversations = chatHistoryRepository.getMyConversations();
        return Result.success(conversations);
    }

    /**
     * 获取指定会话的消息详情
     * @param conversationId 会话ID
     * @return 返回指定会话的所有消息记录
     */
    @GetMapping("/history/{conversationId}")
    @ApiOperation("查看会话消息详情")
    public Result<List<ChatHistory>> getMessages(
            @PathVariable String conversationId) {
        List<ChatHistory> messages = chatHistoryRepository.getConversationMessages(conversationId);
        return Result.success(messages);
    }

    @DeleteMapping("/history/{conversationId}")
    @ApiOperation("删除历史会话")
    public Result<Void> deleteConversation(
            @PathVariable String conversationId) {
        chatHistoryRepository.deleteConversation(conversationId);
        return Result.success();
    }
}
