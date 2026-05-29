// 定义包路径
package com.campus.trade.config;

// 导入AI配置属性类
import com.campus.trade.properties.AiProperties;
// 导入AI工具服务类
import com.campus.trade.service.AiToolService;
// 导入SLF4J日志记录器接口
import org.slf4j.Logger;
// 导入SLF4J日志工厂类
import org.slf4j.LoggerFactory;
// 导入Spring AI聊天客户端
import org.springframework.ai.chat.client.ChatClient;
// 导入消息对话记忆顾问
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
// 导入对话记忆接口
import org.springframework.ai.chat.memory.ChatMemory;
// 导入元数据模式枚举
import org.springframework.ai.document.MetadataMode;
// 导入嵌入模型接口
import org.springframework.ai.embedding.EmbeddingModel;
// 导入OpenAI嵌入模型实现
import org.springframework.ai.openai.OpenAiEmbeddingModel;
// 导入OpenAI嵌入选项配置
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
// 导入OpenAI API客户端
import org.springframework.ai.openai.api.OpenAiApi;
// 导入工具回调支持类
import org.springframework.ai.support.ToolCallbacks;
// 导入向量存储接口
import org.springframework.ai.vectorstore.VectorStore;
// 导入Redis向量存储实现
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
// 导入Spring自动注入注解
import org.springframework.beans.factory.annotation.Autowired;
// 导入条件化配置注解
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// 导入Bean注解
import org.springframework.context.annotation.Bean;
// 导入配置类注解
import org.springframework.context.annotation.Configuration;
// 导入Jedis客户端默认配置
import redis.clients.jedis.DefaultJedisClientConfig;
// 导入Redis主机和端口类
import redis.clients.jedis.HostAndPort;
// 导入Jedis连接池类
import redis.clients.jedis.JedisPooled;

/**
 * AI配置类，负责创建和管理AI服务所需的各种Bean组件。
 * 使用条件化配置确保只在需要时初始化相应的组件。
 */
@Configuration
// 标识该类为Spring配置类
public class AiConfig {

    /**
     * 日志记录器，用于记录配置初始化过程和运行时信息。
     */
    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);
    // 创建静态日志记录器实例

    /**
     * AI配置属性对象，包含所有AI服务相关的配置项。
     */
    @Autowired
    // 自动注入AI配置属性
    private AiProperties aiProperties;
    // AI配置属性字段

    /**
     * 创建并配置ChatClient Bean，用于处理对话交互。
     * <p>
     * 该聊天客户端集成了持久化记忆功能和工具调用能力，
     * 能够维护对话上下文并执行自定义的AI工具。
     * </p>
     *
     * @param builder ChatClient构建器，由Spring AI自动注入
     * @param chatMemory 对话记忆组件，用于保存对话历史
     * @param aiToolService AI工具服务，提供可调用的工具方法
     * @return 配置完成的ChatClient实例
     */
    @Bean
    // 声明该方法返回的对象为Spring管理的Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory, AiToolService aiToolService) {
        // 定义聊天客户端Bean
        return builder
        // 使用构建器模式创建ChatClient
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
        // 配置默认顾问：添加消息对话记忆功能
                .defaultTools(ToolCallbacks.from(aiToolService))
        // 配置默认工具：从AI工具服务创建工具回调
                .build();
        // 构建并返回ChatClient实例
    }

    /**
     * 创建并配置EmbeddingModel Bean，用于将文本转换为向量以支持语义搜索功能。
     * <p>
     * 该方法会根据配置条件性地创建OpenAI嵌入模型实例。API密钥优先使用专用的embeddingApiKey，
     * 如果未配置则回退使用通用的apiKey。
     * </p>
     *
     * @return EmbeddingModel实例，用于文本向量化处理
     *         当sky.ai.embedding-enabled配置为true时才会创建此Bean
     */
    @Bean
    // 声明该方法返回的对象为Spring管理的Bean
    @ConditionalOnProperty(name = "sky.ai.embedding-enabled", havingValue = "true")
    // 条件化配置：仅在sky.ai.embedding-enabled=true时启用
    public EmbeddingModel embeddingModel() {
        // 定义嵌入模型Bean

        String apiKey = aiProperties.getEmbeddingApiKey() != null
        // 检查专用嵌入API密钥是否不为空
                && !aiProperties.getEmbeddingApiKey().isEmpty()
        // 检查专用嵌入API密钥是否不为空字符串
                ? aiProperties.getEmbeddingApiKey()
        // 如果专用密钥存在则使用专用密钥
                : aiProperties.getApiKey();
        // 否则回退使用通用API密钥

        OpenAiApi openAiApi = OpenAiApi.builder()
        // 使用构建器模式创建OpenAI API客户端
                .baseUrl(aiProperties.getEmbeddingBaseUrl())
        // 配置嵌入服务的基础URL
                .apiKey(apiKey)
        // 配置API密钥
                .build();
        // 构建OpenAiApi实例
        OpenAiEmbeddingOptions options = new OpenAiEmbeddingOptions();
        // 创建OpenAI嵌入选项配置对象
        options.setModel(aiProperties.getEmbeddingModel());
        // 设置使用的嵌入模型名称
        log.info("EmbeddingModel initialized: model={}, baseUrl={}",
        // 记录初始化日志
                aiProperties.getEmbeddingModel(), aiProperties.getEmbeddingBaseUrl());
        // 日志参数：模型名称和基础URL
        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options);
        // 创建并返回OpenAI嵌入模型实例
    }

    /**
     * 创建Jedis连接池Bean，专用于RedisVectorStore的数据访问。
     * <p>
     * 这是一个独立的Jedis连接池，不会与现有的基于Lettuce的
     * spring-boot-starter-data-redis产生冲突。两个客户端可以连接到相同的Redis
     * 实例，或者根据配置连接到不同的实例。
     * </p>
     *
     * @return JedisPooled连接池实例
     *         当sky.ai.embedding-enabled配置为true时才会创建此Bean
     */
    @Bean
    // 声明该方法返回的对象为Spring管理的Bean
    @ConditionalOnProperty(name = "sky.ai.embedding-enabled", havingValue = "true")
    // 条件化配置：仅在sky.ai.embedding-enabled=true时启用
    public JedisPooled jedisPooled() {
        // 定义Jedis连接池Bean
        String host = aiProperties.getVectorRedisHost();
        // 获取Redis服务器主机地址
        int port = aiProperties.getVectorRedisPort();
        // 获取Redis服务器端口号
        String password = aiProperties.getVectorRedisPassword();
        // 获取Redis访问密码
        int database = aiProperties.getVectorRedisDatabase();
        // 获取Redis数据库索引

        DefaultJedisClientConfig.Builder configBuilder = DefaultJedisClientConfig.builder();
        // 创建Jedis客户端配置构建器
        if (password != null && !password.isEmpty()) {
        // 判断是否配置了密码
            configBuilder.password(password);
        // 如果配置了密码则设置到配置构建器中
        }
        configBuilder.database(database);
        // 设置要连接的数据库索引

        JedisPooled pool = new JedisPooled(new HostAndPort(host, port), configBuilder.build());
        // 创建Jedis连接池实例
        log.info("JedisPooled initialized for VectorStore: {}:{}/{}", host, port, database);
        // 记录连接池初始化日志
        return pool;
        // 返回Jedis连接池实例
    }

    /**
     * 创建RedisVectorStore Bean，提供生产级别的持久化向量存储服务。
     * <p>
     * 将菜单项的嵌入向量存储在Redis Stack中（需要RediSearch模块支持）。
     * 数据在应用重启后仍然保留，支持多个应用实例共享同一个向量存储。
     * </p>
     *
     * @param jedisPooled Jedis连接池，由Spring容器自动注入
     * @param embeddingModel 嵌入模型，用于生成文本向量
     * @return RedisVectorStore实例，用于向量数据的存储和检索
     *         当sky.ai.embedding-enabled配置为true时才会创建此Bean
     */
    @Bean
    // 声明该方法返回的对象为Spring管理的Bean
    @ConditionalOnProperty(name = "sky.ai.embedding-enabled", havingValue = "true")
    // 条件化配置：仅在sky.ai.embedding-enabled=true时启用
    public VectorStore vectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {
        // 定义向量存储Bean
        RedisVectorStore store = RedisVectorStore.builder(jedisPooled, embeddingModel)
        // 使用构建器模式创建Redis向量存储
                .indexName(aiProperties.getVectorIndexName())
        // 配置向量索引名称
                .prefix(aiProperties.getVectorPrefix())
        // 配置键名前缀
                .initializeSchema(aiProperties.isVectorInitSchema())
        // 配置是否自动初始化 schema
                .build();
        // 构建RedisVectorStore实例
        log.info("RedisVectorStore initialized: index={}, prefix={}",
        // 记录向量存储初始化日志
                aiProperties.getVectorIndexName(), aiProperties.getVectorPrefix());
        // 日志参数：索引名称和前缀
        return store;
        // 返回RedisVectorStore实例
    }
}
