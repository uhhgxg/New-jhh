package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.ai")
@Data
public class AiProperties {

    /** 阿里云百炼 DashScope API Key */
    private String apiKey;

    /** API 基础地址（百炼 DashScope OpenAI 兼容端点） */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    /** 默认对话模型 */
    private String model = "qwen-plus";

    /** 向量模型名称 */
    private String embeddingModel = "text-embedding-v2";

    /** 向量模型 API 基础地址（百炼同端点） */
    private String embeddingBaseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    /** embedding API Key (reuses chat apiKey when empty) */
    private String embeddingApiKey;

    /** enable vector store and RAG */
    private boolean embeddingEnabled = false;

    // ────────────── Redis Vector Store ──────────────

    /** Redis host for vector store */
    private String vectorRedisHost = "localhost";

    /** Redis port for vector store */
    private int vectorRedisPort = 6379;

    /** Redis password for vector store */
    private String vectorRedisPassword;

    /** Redis database for vector store */
    private int vectorRedisDatabase = 0;

    /** Redis vector index name */
    private String vectorIndexName = "menu_vectors";

    /** Redis key prefix for vector documents */
    private String vectorPrefix = "menu_doc";

    /** auto-create index schema on startup */
    private boolean vectorInitSchema = true;

    // ────────────── Prompt templates ──────────────

    /** AI assistant role setting */
    private String rolePrompt = "You are the smart assistant Xiao Tuantuan for Sky Takeout, a warm and cute ordering assistant.";

    /** AI assistant duty description */
    private String dutyPrompt = "1. Recommend dishes based on user needs.\n2. Answer questions about dishes, orders, delivery, payment.\n3. Only answer restaurant-related questions.\n4. Reply concisely and friendly in Chinese.";

    /**
     * RAG prompt template.
     * Variables: {context} = retrieved menu items, {question} = user query.
     * The LLM is instructed to ground its answer in the retrieved context.
     */
    private String ragPrompt = """
            You are the smart assistant for Sky Takeout. Below are relevant menu items retrieved from our database. Use ONLY this context to answer the user's question. If the context does not contain enough information, say so honestly and do not make up dishes.

            ## Retrieved menu context
            {context}

            ## User question
            {question}

            ## Instructions
            1. Answer based strictly on the retrieved context above.
            2. Recommend specific dishes/set meals with prices when relevant.
            3. Keep the tone warm, cute, and enthusiastic (like a friendly waiter/waitress).
            4. Reply in Chinese, concise and helpful.
            5. If the context is insufficient, politely explain what you can and cannot answer.
            6. Use emoji sparingly to keep it friendly.""";
}
