package com.campus.trade.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "campus.ai")
@Data
public class AiProperties {

    private String apiKey;

    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    private String model = "qwen-plus";

    private String embeddingModel = "text-embedding-v2";

    private String embeddingBaseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    private String embeddingApiKey;

    private boolean embeddingEnabled = false;

    private String vectorRedisHost = "localhost";

    private int vectorRedisPort = 6379;

    private String vectorRedisPassword;

    private int vectorRedisDatabase = 0;

    private String vectorIndexName = "item_vectors";

    private String vectorPrefix = "item_doc";

    private boolean vectorInitSchema = true;

    private String rolePrompt = "You are the smart assistant for Campus Second-hand Trading Platform, a warm and cute trading assistant.";

    private String dutyPrompt = "1. Recommend second-hand items based on user needs.\n2. Answer questions about items, orders, payment, shipping.\n3. Only answer campus-trading related questions.\n4. Reply concisely and friendly in Chinese.";

    private String ragPrompt = "You are the smart assistant for Campus Second-hand Trading Platform. Below are relevant items retrieved from our database.\n\nRetrieved context:\n{context}\n\nUser question:\n{question}\n\nInstructions:\n1. Answer based strictly on the retrieved context.\n2. Recommend specific items with prices and condition when relevant.\n3. Keep the tone warm, cute, and enthusiastic.\n4. Reply in Chinese, concise and helpful.\n5. If context is insufficient, say so honestly.";
}
