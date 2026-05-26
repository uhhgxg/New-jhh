package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {

/**
 * 配置ServerEndpointExporter，这个bean会自动注册WebSocket的endpoint
 * 它会扫描带有@ServerEndpoint注解的类，并将它们注册为WebSocket端点
 *
 * @return ServerEndpointExporter 实例，用于导出WebSocket端点
 */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
    // 创建并返回ServerEndpointExporter实例
    // 这个Bean是WebSocket配置所必需的，用于注册WebSocket端点
        return new ServerEndpointExporter();
    }
}
