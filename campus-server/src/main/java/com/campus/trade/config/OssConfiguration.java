package com.campus.trade.config;


import com.campus.trade.properties.AliOssProperties;
import com.campus.trade.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OSS配置类
 * 用于配置和注册阿里云OSS相关的Bean
 */
@Configuration
@Slf4j

public class OssConfiguration {
    /**
     * 创建并注册AliOssUtil Bean
     * 当容器中不存在该Bean时才会创建
     * @param aliOssProperties 阿里云OSS属性配置
     * @return AliOssUtil实例
     */
    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil alossUtil(AliOssProperties aliOssProperties) {
        log.info("开始注册阿里云OSS..."); // 记录开始注册OSS的日志信息
        return new AliOssUtil(aliOssProperties.getEndpoint(), // 创建AliOssUtil实例，传入OSS终端节点
                aliOssProperties.getAccessKeyId(), // 传入访问密钥ID
                aliOssProperties.getAccessKeySecret(), // 传入访问密钥密码
                aliOssProperties.getBucketName()); // 传入存储桶名称
    }

    }


