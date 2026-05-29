/**
 * AI助手服务实现类，提供聊天、向量搜索和RAG（检索增强生成）功能。
 *
 * <h3>模式路由</h3>
 * <ul>
 *   <li>{@code chat}（默认）— 直接LLM对话，系统提示包含完整商品信息</li>
 *   <li>{@code vector} — 嵌入查询，对商品向量进行语义搜索，返回排序结果</li>
 *   <li>{@code rag} — 嵌入查询 → 检索top K商品项 → 注入到提示词 → LLM生成回答
 *       基于真实商品数据，推荐准确性最佳</li>
 * </ul>
 *
 * <h3>存储</h3>
 * 商品嵌入通过{@link VectorStore}（Redis Stack + RediSearch）持久化存储在Redis中。
 * 应用重启后数据仍然存活，可在水平扩展的多个实例间共享。
 */
package com.campus.trade.service.impl;

// 导入消息常量类
import com.campus.trade.constant.MessageConstant;
// 导入状态常量类
import com.campus.trade.constant.StatusConstant;
// 导入基础上下文类，用于获取当前用户ID
import com.campus.trade.context.BaseContext;
// 导入AI聊天数据传输对象
import com.campus.trade.dto.AiChatDTO;
// 导入分类实体类
import com.campus.trade.entity.Category;
// 导入商品实体类
import com.campus.trade.entity.Item;
// 导入捆绑包实体类
import com.campus.trade.entity.Bundle;
// 导入基础异常类
import com.campus.trade.exception.BaseException;
// 导入分类Mapper接口
import com.campus.trade.mapper.CategoryMapper;
// 导入商品Mapper接口
import com.campus.trade.mapper.ItemMapper;
// 导入捆绑包Mapper接口
import com.campus.trade.mapper.BundleMapper;
// 导入AI配置属性类
import com.campus.trade.properties.AiProperties;
// 导入AI助手服务接口
import com.campus.trade.service.AiAssistantService;
// 导入AI聊天视图对象
import com.campus.trade.vo.AiChatVO;
// 导入SLF4J日志注解
import lombok.extern.slf4j.Slf4j;
// 导入Spring AI聊天客户端
import org.springframework.ai.chat.client.ChatClient;
// 导入对话记忆接口
import org.springframework.ai.chat.memory.ChatMemory;
// 导入文档类，用于向量存储
import org.springframework.ai.document.Document;
// 导入嵌入模型接口
import org.springframework.ai.embedding.EmbeddingModel;
// 导入搜索请求构建器
import org.springframework.ai.vectorstore.SearchRequest;
// 导入向量存储接口
import org.springframework.ai.vectorstore.VectorStore;
// 导入Spring自动注入注解
import org.springframework.beans.factory.annotation.Autowired;
// 导入Spring生命周期注解
import jakarta.annotation.PostConstruct;
// 导入Spring定时任务注解
import org.springframework.scheduling.annotation.Scheduled;
// 导入Spring服务注解
import org.springframework.stereotype.Service;
// 导入响应式流发布者
import reactor.core.publisher.Flux;

// 导入ArrayList集合类
import java.util.ArrayList;
// 导入HashMap映射类
import java.util.HashMap;
// 导入List接口
import java.util.List;
// 导入Map接口
import java.util.Map;
// 导入UUID生成器
import java.util.UUID;
// 导入流收集器工具类
import java.util.stream.Collectors;

/**
 * AI助手服务实现类，提供聊天、向量搜索和RAG（检索增强生成）功能。
 *
 * <h3>模式路由</h3>
 * <ul>
 *   <li>{@code chat}（默认）— 直接LLM对话，系统提示包含完整商品信息</li>
 *   <li>{@code vector} — 嵌入查询，对商品向量进行语义搜索，返回排序结果</li>
 *   <li>{@code rag} — 嵌入查询 → 检索top K商品项 → 注入到提示词 → LLM生成回答
 *       基于真实商品数据，推荐准确性最佳</li>
 * </ul>
 *
 * <h3>存储</h3>
 * 商品嵌入通过{@link VectorStore}（Redis Stack + RediSearch）持久化存储在Redis中。
 * 应用重启后数据仍然存活，可在水平扩展的多个实例间共享。
 */
@Service
// 标记该类为Spring服务组件
@Slf4j
// 启用日志记录功能
public class AiAssistantServiceImpl implements AiAssistantService {

    @Autowired
    // 自动注入聊天客户端
    private ChatClient chatClient;

    @Autowired(required = false)
    // 可选地自动注入嵌入模型（可能不存在）
    private EmbeddingModel embeddingModel;

    @Autowired(required = false)
    // 可选地自动注入向量存储（可能不存在）
    private VectorStore vectorStore;

    @Autowired
    // 自动注入商品Mapper
    private ItemMapper itemMapper;

    @Autowired
    // 自动注入捆绑包Mapper
    private BundleMapper bundleMapper;

    @Autowired
    // 自动注入分类Mapper
    private CategoryMapper categoryMapper;

    @Autowired
    // 自动注入AI配置属性
    private AiProperties aiProperties;

    /** 从向量存储中检索的文档数量 */
    private static final int VECTOR_TOP_K = 5;

    /** 文档被视为相关的最低相似度阈值 */
    private static final double VECTOR_SIMILARITY_THRESHOLD = 0.3;

    /** 商品索引是否已在Redis中构建（启动自动构建 + 定时重建） */
    private volatile boolean indexReady = false;

    // ═══════════════════════════════════════════════════════════════════════
    // 公共API — 模式路由
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    // 实现接口方法：处理AI聊天请求
    public AiChatVO chat(AiChatDTO dto) {
        return switch (mode(dto)) {
            case "rag"   -> ragChat(dto);
            // RAG模式：检索增强生成
            case "vector" -> vectorSearch(dto);
            // 向量搜索模式：仅检索不生成
            default       -> doChat(dto);
            // 默认聊天模式：直接LLM对话
        };
    }

    @Override
    // 实现接口方法：处理AI流式聊天请求
    public Flux<String> chatStream(AiChatDTO dto) {
        String m = mode(dto);
        // 获取聊天模式
        if ("rag".equals(m) || "vector".equals(m)) {
            return Flux.just(m + " mode does not support streaming. Use the /chat endpoint with POST.");
            // RAG和向量搜索模式不支持流式输出，返回提示信息
        }
        return doChatStream(dto);
        // 执行流式聊天
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 聊天（直接LLM）
    // ═══════════════════════════════════════════════════════════════════════

    private AiChatVO doChat(AiChatDTO dto) {
        String conversationId = resolveId(dto);
        // 解析或生成对话ID
        String userMessage = dto.getMessage().trim();
        // 获取并修剪用户消息
        String systemPrompt = buildSystemPrompt();
        // 构建系统提示词

        try {
            String reply = chatClient.prompt()
                    .system(systemPrompt)
                    // 设置系统提示词
                    .user(userMessage)
                    // 设置用户消息
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    // 添加对话记忆顾问，传入对话ID
                    .call()
                    // 调用LLM
                    .content();
            // 获取回复内容

            return AiChatVO.builder()
                    .reply(reply)
                    // 设置回复内容
                    .conversationId(conversationId)
                    // 设置对话ID
                    .model(aiProperties.getModel())
                    // 设置使用的模型
                    .build();
            // 构建并返回AI聊天视图对象
        } catch (Exception e) {
            log.error("Chat failed — conversationId={}, userId={}",
                    conversationId, BaseContext.getCurrentId(), e);
            // 记录错误日志
            throw new BaseException(resolveErrorMessage(e));
            // 抛出业务异常
        }
    }

    private Flux<String> doChatStream(AiChatDTO dto) {
        String conversationId = resolveId(dto);
        // 解析或生成对话ID
        String userMessage = dto.getMessage().trim();
        // 获取并修剪用户消息
        String systemPrompt = buildSystemPrompt();
        // 构建系统提示词

        try {
            return chatClient.prompt()
                    .system(systemPrompt)
                    // 设置系统提示词
                    .user(userMessage)
                    // 设置用户消息
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    // 添加对话记忆顾问，传入对话ID
                    .stream()
                    // 以流式方式调用LLM
                    .content()
                    // 获取内容流
                    .doOnError(e -> log.error("Stream chat failed — conversationId={}, userId={}",
                            conversationId, BaseContext.getCurrentId(), e))
                    // 发生错误时记录日志
                    .onErrorResume(e -> Flux.error(
                            new BaseException(resolveErrorMessage((Exception) e))));
            // 错误恢复：将异常转换为Flux.error
        } catch (Exception e) {
            log.error("Stream chat failed — conversationId={}, userId={}",
                    conversationId, BaseContext.getCurrentId(), e);
            // 记录错误日志
            return Flux.error(new BaseException(resolveErrorMessage(e)));
            // 返回错误流
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // RAG — 检索 + 增强 + 生成
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 完整的RAG流水线：
     * <ol>
     *   <li>确保商品向量索引已在Redis中构建</li>
     *   <li>嵌入用户查询并检索最相似的top K商品项</li>
     *   <li>将检索到的项格式化为结构化上下文字符串</li>
     *   <li>将上下文和问题注入RAG提示词模板</li>
     *   <li>调用LLM生成基于事实的自然语言回答</li>
     * </ol>
     */
    private AiChatVO ragChat(AiChatDTO dto) {
        ensureVectorStoreReady();
        // 确保向量存储已就绪
        String conversationId = resolveId(dto);
        // 解析或生成对话ID
        String question = dto.getMessage().trim();
        // 获取并修剪用户问题

        try {
            // 1. 从Redis向量存储中检索相关商品项
            List<Document> retrieved = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(question)
                            // 设置查询文本
                            .topK(VECTOR_TOP_K)
                            // 设置返回结果数量
                            .similarityThreshold(VECTOR_SIMILARITY_THRESHOLD)
                            // 设置相似度阈值
                            .build());
            // 执行相似度搜索

            // 2. 构建上下文字符串和匹配项
            String context = buildContextFromDocuments(retrieved);
            // 从文档构建上下文字符串
            List<AiChatVO.VectorMatchItem> matches = buildMatchItems(retrieved);
            // 构建匹配项列表

            // 3. 填充RAG提示词模板
            String ragSystemPrompt = aiProperties.getRagPrompt()
                    .replace("{context}", context)
                    // 替换上下文占位符
                    .replace("{question}", question);
            // 替换问题占位符

            // 4. 使用增强后的提示词调用LLM
            String reply = chatClient.prompt()
                    .system(ragSystemPrompt)
                    // 设置RAG系统提示词
                    .user(question)
                    // 设置用户问题
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    // 添加对话记忆顾问
                    .call()
                    // 调用LLM
                    .content();
            // 获取回复内容

            return AiChatVO.builder()
                    .reply(reply)
                    // 设置回复内容
                    .conversationId(conversationId)
                    // 设置对话ID
                    .model(aiProperties.getModel())
                    // 设置使用的模型
                    .matches(matches)
                    // 设置匹配项列表
                    .build();
            // 构建并返回AI聊天视图对象
        } catch (BaseException e) {
            throw e;
            // 直接抛出业务异常
        } catch (Exception e) {
            log.error("RAG failed — conversationId={}, userId={}, question={}",
                    conversationId, BaseContext.getCurrentId(), question, e);
            // 记录错误日志
            throw new BaseException(MessageConstant.AI_SERVICE_ERROR);
            // 抛出AI服务错误异常
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 向量搜索（仅检索，不调用LLM）
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    // 实现接口方法：执行向量搜索
    public AiChatVO vectorSearch(AiChatDTO dto) {
        ensureVectorStoreReady();
        // 确保向量存储已就绪
        String conversationId = resolveId(dto);
        // 解析或生成对话ID
        String query = dto.getMessage().trim();
        // 获取并修剪查询文本

        try {
            List<Document> results = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(query)
                            // 设置查询文本
                            .topK(VECTOR_TOP_K)
                            // 设置返回结果数量
                            .similarityThreshold(VECTOR_SIMILARITY_THRESHOLD)
                            // 设置相似度阈值
                            .build());
            // 执行相似度搜索

            List<AiChatVO.VectorMatchItem> matches = buildMatchItems(results);
            // 构建匹配项列表

            String reply;
            // 声明回复变量
            if (matches.isEmpty()) {
                reply = "No matching items or bundles found for '" + query
                        + "'. Try a different description.";
                // 如果没有匹配项，返回提示信息
            } else {
                StringBuilder sb = new StringBuilder();
                // 创建字符串构建器
                sb.append("Based on '").append(query).append("', here are the recommendations:\n\n");
                // 添加标题
                for (int i = 0; i < matches.size(); i++) {
                    AiChatVO.VectorMatchItem item = matches.get(i);
                    // 获取当前匹配项
                    sb.append(i + 1).append(". **").append(item.getName()).append("**");
                    // 添加序号和名称
                    if (!item.getPrice().isEmpty()) {
                        sb.append(" ($").append(item.getPrice()).append(")");
                        // 如果价格不为空，添加价格
                    }
                    sb.append("\n");
                    // 换行
                    if (!item.getCategory().isEmpty()) {
                        sb.append("   Category: ").append(item.getCategory()).append("\n");
                        // 如果分类不为空，添加分类
                    }
                    if (!item.getDescription().isEmpty()) {
                        sb.append("   ").append(item.getDescription()).append("\n");
                        // 如果描述不为空，添加描述
                    }
                    sb.append("\n");
                    // 每个项目之间空一行
                }
                reply = sb.toString().trim();
                // 转换为字符串并去除首尾空白
            }

            return AiChatVO.builder()
                    .reply(reply)
                    // 设置回复内容
                    .conversationId(conversationId)
                    // 设置对话ID
                    .model(aiProperties.getEmbeddingModel())
                    // 设置使用的嵌入模型
                    .matches(matches)
                    // 设置匹配项列表
                    .build();
            // 构建并返回AI聊天视图对象
        } catch (BaseException e) {
            throw e;
            // 直接抛出业务异常
        } catch (Exception e) {
            log.error("Vector search failed — conversationId={}, userId={}, query={}",
                    conversationId, BaseContext.getCurrentId(), query, e);
            // 记录错误日志
            throw new BaseException(MessageConstant.AI_SERVICE_ERROR);
            // 抛出AI服务错误异常
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 商品向量索引管理 — 启动自动构建 + 定时重建
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 启动时自动构建商品向量索引。
     * 仅当嵌入功能启用（vectorStore 已注入）时执行。
     */
    @PostConstruct
    public void initVectorIndex() {
        if (vectorStore == null) {
            log.info("Vector store disabled — skipping index build on startup");
            return;
        }
        doRebuildIndex();
        indexReady = true;
    }

    /**
     * 定时自动重建商品向量索引（每30分钟），确保商品变更后索引保持同步。
     */
    @Scheduled(fixedRate = 1_800_000, initialDelay = 1_800_000)
    public void scheduledRebuildIndex() {
        if (vectorStore == null) {
            return;
        }
        log.info("Scheduled vector index rebuild triggered");
        doRebuildIndex();
    }

    /**
     * 强制手动重建索引（供管理端 API 调用或紧急修复使用）。
     */
    public void forceRebuildIndex() {
        doRebuildIndex();
    }

    private void ensureVectorStoreReady() {
        if (vectorStore == null) {
            throw new BaseException(
                    "Vector store is not enabled. Set sky.ai.embedding-enabled=true in config.");
        }
    }

    private synchronized void doRebuildIndex() {
        List<Category> categories = categoryMapper.list(null);
        // 查询所有分类
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        // 构建分类ID到名称的映射

        List<Document> documents = new ArrayList<>();
        // 创建文档列表

        // Items
        // 处理商品
        Item itemCondition = Item.builder().saleStatus(StatusConstant.ON_SALE).build();
        // 构建查询条件：状态为上架的商品
        for (Item item : itemMapper.list(itemCondition)) {
            String categoryName = categoryMap.getOrDefault(item.getCategoryId(), "Uncategorized");
            // 获取分类名称，默认为"Uncategorized"
            String content = buildDocumentContent(item.getItemName(), categoryName,
                    item.getItemDescription(), "item");
            // 构建文档内容
            Map<String, Object> meta = new HashMap<>();
            // 创建元数据映射
            meta.put("name", item.getItemName());
            // 添加名称
            meta.put("price", item.getUnitPrice() != null ? item.getUnitPrice().toString() : "");
            // 添加价格
            meta.put("category", categoryName);
            // 添加分类
            meta.put("description", item.getItemDescription() != null ? item.getItemDescription() : "");
            // 添加描述
            meta.put("type", "item");
            // 添加类型
            documents.add(new Document(content, meta));
            // 添加到文档列表
        }

        // Bundles
        // 处理捆绑包
        Bundle bundleCondition = Bundle.builder().status(StatusConstant.ENABLE).build();
        // 构建查询条件：状态为启用的捆绑包
        for (Bundle bundle : bundleMapper.list(bundleCondition)) {
            String categoryName = categoryMap.getOrDefault(bundle.getCategoryId(), "Uncategorized");
            // 获取分类名称，默认为"Uncategorized"
            String content = buildDocumentContent(bundle.getName(), categoryName,
                    bundle.getDescription(), "bundle");
            // 构建文档内容
            Map<String, Object> meta = new HashMap<>();
            // 创建元数据映射
            meta.put("name", bundle.getName());
            // 添加名称
            meta.put("price", bundle.getPrice() != null ? bundle.getPrice().toString() : "");
            // 添加价格
            meta.put("category", categoryName);
            // 添加分类
            meta.put("description", bundle.getDescription() != null ? bundle.getDescription() : "");
            // 添加描述
            meta.put("type", "bundle");
            // 添加类型
            documents.add(new Document(content, meta));
            // 添加到文档列表
        }

        if (!documents.isEmpty()) {
            vectorStore.add(documents);
            // 将文档添加到向量存储
            log.info("Item vector index built: {} documents written to Redis", documents.size());
            // 记录成功日志
        } else {
            log.warn("Item vector index is empty — no on-sale items or enabled bundles found");
            // 记录警告日志：没有上架中的商品或捆绑包
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 辅助方法
    // ═══════════════════════════════════════════════════════════════════════

    /** 从DTO中提取模式，默认为"chat" */
    private static String mode(AiChatDTO dto) {
        String m = dto.getMode();
        // 获取模式参数
        return (m != null && !m.isEmpty()) ? m.toLowerCase() : "chat";
        // 如果为空则返回"chat"
    }

    /** 解析或生成对话ID */
    private String resolveId(AiChatDTO dto) {
        return dto.getConversationId() != null ? dto.getConversationId() : UUID.randomUUID().toString();
        // 如果已有对话ID则使用，否则生成新的UUID
    }

    /**
     * 构建将要被嵌入的单个商品项文本。
     * 这是向量相似度搜索匹配的内容。
     */
    private String buildDocumentContent(String name, String category, String description, String type) {
        StringBuilder sb = new StringBuilder();
        // 创建字符串构建器
        sb.append(type).append(": ").append(name);
        // 添加类型和名称
        sb.append(", Category: ").append(category);
        // 添加分类
        if (description != null && !description.isEmpty()) {
            sb.append(", Description: ").append(description);
            // 如果描述不为空，添加描述
        }
        return sb.toString();
        // 返回构建的内容
    }

    /**
     * 将检索到的文档转换为RAG提示词的结构化上下文字符串。
     */
    private String buildContextFromDocuments(List<Document> docs) {
        if (docs.isEmpty()) {
            return "(No matching items found)";
            // 如果没有文档，返回提示信息
        }
        StringBuilder sb = new StringBuilder();
        // 创建字符串构建器
        for (int i = 0; i < docs.size(); i++) {
            Document doc = docs.get(i);
            // 获取当前文档
            Map<String, Object> meta = doc.getMetadata();
            // 获取元数据
            sb.append(i + 1).append(". ");
            // 添加序号
            sb.append(meta.getOrDefault("name", "Unknown"));
            // 添加名称
            String price = (String) meta.getOrDefault("price", "");
            // 获取价格
            if (!price.isEmpty()) sb.append(" — $").append(price);
            // 如果价格不为空，添加价格
            String cat = (String) meta.getOrDefault("category", "");
            // 获取分类
            if (!cat.isEmpty()) sb.append(" — ").append(cat);
            // 如果分类不为空，添加分类
            String desc = (String) meta.getOrDefault("description", "");
            // 获取描述
            if (!desc.isEmpty()) sb.append(" — ").append(desc);
            // 如果描述不为空，添加描述
            sb.append(" (score: ").append(String.format("%.2f", doc.getScore() != null ? doc.getScore() : 0)).append(")");
            // 添加相似度分数
            sb.append("\n");
            // 换行
        }
        return sb.toString();
        // 返回构建的上下文
    }

    /** 将检索到的文档转换为API响应的VO匹配项 */
    private List<AiChatVO.VectorMatchItem> buildMatchItems(List<Document> docs) {
        List<AiChatVO.VectorMatchItem> matches = new ArrayList<>();
        // 创建匹配项列表
        for (Document doc : docs) {
            Map<String, Object> meta = doc.getMetadata();
            // 获取元数据
            matches.add(AiChatVO.VectorMatchItem.builder()
                    .name((String) meta.getOrDefault("name", ""))
                    // 设置名称
                    .price((String) meta.getOrDefault("price", ""))
                    // 设置价格
                    .category((String) meta.getOrDefault("category", ""))
                    // 设置分类
                    .description((String) meta.getOrDefault("description", ""))
                    // 设置描述
                    .type((String) meta.getOrDefault("type", ""))
                    // 设置类型
                    .score(doc.getScore() != null ? doc.getScore() : 0.0)
                    // 设置相似度分数
                    .build());
            // 构建并添加到列表
        }
        return matches;
        // 返回匹配项列表
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 直接聊天模式的系统提示词
    // ═══════════════════════════════════════════════════════════════════════

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        // 创建字符串构建器
        sb.append(aiProperties.getRolePrompt()).append("\n\n");
        // 添加角色提示词
        sb.append("## Current Item Catalog\n");
        // 添加商品目录标题

        List<Category> categories = categoryMapper.list(null);
        // 查询所有分类
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        // 构建分类ID到名称的映射

        Item itemCondition = Item.builder().saleStatus(StatusConstant.ON_SALE).build();
        // 构建查询条件：状态为上架的商品
        List<Item> items = itemMapper.list(itemCondition);
        // 查询所有上架的商品
        if (!items.isEmpty()) {
            sb.append("### Items\n");
            // 添加商品子标题
            for (Item item : items) {
                String categoryName = categoryMap.getOrDefault(item.getCategoryId(), "Uncategorized");
                // 获取分类名称
                sb.append("- ").append(item.getItemName())
                        .append(" ($").append(item.getUnitPrice()).append(")")
                        .append(" - ").append(categoryName);
                // 添加商品名称、价格和分类
                if (item.getItemDescription() != null && !item.getItemDescription().isEmpty()) {
                    sb.append(" - ").append(item.getItemDescription());
                    // 如果描述不为空，添加描述
                }
                sb.append("\n");
                // 换行
            }
        }

        Bundle bundleCondition = Bundle.builder().status(StatusConstant.ENABLE).build();
        // 构建查询条件：状态为启用的捆绑包
        List<Bundle> bundles = bundleMapper.list(bundleCondition);
        // 查询所有启用的捆绑包
        if (!bundles.isEmpty()) {
            sb.append("### Bundles\n");
            // 添加捆绑包子标题
            for (Bundle bundle : bundles) {
                String categoryName = categoryMap.getOrDefault(bundle.getCategoryId(), "Uncategorized");
                // 获取分类名称
                sb.append("- ").append(bundle.getName())
                        .append(" ($").append(bundle.getPrice()).append(")")
                        .append(" - ").append(categoryName);
                // 添加捆绑包名称、价格和分类
                if (bundle.getDescription() != null && !bundle.getDescription().isEmpty()) {
                    sb.append(" - ").append(bundle.getDescription());
                    // 如果描述不为空，添加描述
                }
                sb.append("\n");
                // 换行
            }
        }

        sb.append("\n## Your Duties\n");
        // 添加职责标题
        sb.append(aiProperties.getDutyPrompt()).append("\n");
        // 添加职责提示词
        return sb.toString();
        // 返回构建的系统提示词
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 错误处理
    // ═══════════════════════════════════════════════════════════════════════

    private String resolveErrorMessage(Exception e) {
        String msg = traverseMessage(e).toLowerCase();
        // 遍历异常消息并转换为小写
        if (msg.contains("timeout") || msg.contains("timed out")
                || msg.contains("read timeout") || msg.contains("connect timeout")) {
            return MessageConstant.AI_TIMEOUT_ERROR;
            // 如果是超时错误，返回超时错误消息
        }
        if (msg.contains("429") || msg.contains("rate limit")
                || msg.contains("too many requests")) {
            return MessageConstant.AI_BUSY_ERROR;
            // 如果是限流错误，返回繁忙错误消息
        }
        if (msg.contains("connection refused") || msg.contains("connect refused")
                || msg.contains("unknown host") || msg.contains("no route to host")) {
            return MessageConstant.AI_CONNECTION_ERROR;
            // 如果是连接错误，返回连接错误消息
        }
        return MessageConstant.AI_SERVICE_ERROR;
        // 默认返回服务错误消息
    }

    private String traverseMessage(Throwable e) {
        StringBuilder sb = new StringBuilder();
        // 创建字符串构建器
        Throwable current = e;
        // 从当前异常开始
        while (current != null) {
            if (current.getMessage() != null) {
                sb.append(current.getMessage()).append(" ");
                // 追加异常消息
            }
            current = current.getCause();
            // 移动到原因异常
        }
        return sb.toString();
        // 返回拼接的消息
    }
}
