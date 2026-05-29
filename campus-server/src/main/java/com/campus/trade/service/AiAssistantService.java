package com.campus.trade.service;

import com.campus.trade.dto.AiChatDTO;
import com.campus.trade.vo.AiChatVO;
import reactor.core.publisher.Flux;

/**
 * AI助手服务接口，定义了与AI助手交互的核心方法
 * 该接口提供了两种聊天方式：普通聊天和流式聊天
 */
public interface AiAssistantService {

    /**
     * 普通聊天方法
     * @param dto 包含聊天请求参数的数据传输对象
     * @return AiChatVO 包含聊天响应的结果对象
     */
    AiChatVO chat(AiChatDTO dto);

    /**
     * 流式聊天方法
     * @param dto 包含聊天请求参数的数据传输对象
     * @return Flux<String> 返回一个响应式流，包含聊天响应的各个片段
     */
    Flux<String> chatStream(AiChatDTO dto);

    /**向量检索方法
     * 将用户查询文本向量化后在商品向量库中检索最匹配的商品/捆绑包
     * @param dto 包含查询参数的数据传输对象
     * @return AiChatVO 包含检索结果和匹配项
     */
    AiChatVO vectorSearch(AiChatDTO dto);
}
