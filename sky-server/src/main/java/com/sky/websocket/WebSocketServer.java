package com.sky.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务器端组件
 * 使用Spring的@Component注解标记为Spring组件
 * 使用@ServerEndpoint注解定义WebSocket端点，路径为/ws/{sid}
 * 使用@Slf4j注解自动生成日志记录器
 */
@Component
@ServerEndpoint("/ws/{sid}")
@Slf4j
public class WebSocketServer {

    // 使用ConcurrentHashMap存储所有连接的WebSocket会话，键为sid
    private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 当WebSocket连接建立时调用
     * @param session WebSocket会话
     * @param sid 客户端唯一标识
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        // 将新连接的会话存入sessionMap
        sessionMap.put(sid, session);
        // 记录客户端连接信息及当前在线数
        log.info("客户端连接：sid={}，当前在线数：{}", sid, sessionMap.size());
    }

    /**
     * 当WebSocket连接关闭时调用
     * @param sid 客户端唯一标识
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        // 从sessionMap中移除已关闭的会话
        sessionMap.remove(sid);
        // 记录客户端断开信息及当前在线数
        log.info("客户端断开：sid={}，当前在线数：{}", sid, sessionMap.size());
    }

    /**
     * 当WebSocket发生错误时调用
     * @param sid 客户端唯一标识
     * @param error 错误信息
     */
    @OnError
    public void onError(@PathParam("sid") String sid, Throwable error) {
        // 发生错误时移除会话
        sessionMap.remove(sid);
        // 记录错误信息
        log.error("WebSocket 错误：sid={}", sid, error);
    }

    /**
     * 向指定客户端发送消息
     * @param sid 目标客户端唯一标识
     * @param message 要发送的消息内容
     * @throws IOException 当发送消息时发生IO异常
     */
    public static void sendMessage(String sid, String message) throws IOException {
        // 从sessionMap获取目标会话
        Session session = sessionMap.get(sid);
        // 检查会话是否存在且已打开
        if (session != null && session.isOpen()) {
            // 发送文本消息
            session.getBasicRemote().sendText(message);
        }
    }

    /**
     * 向所有连接的客户端广播消息
     * @param message 要广播的消息内容
     * @throws IOException 当发送消息时发生IO异常
     */
    public static void broadcast(String message) throws IOException {
        // 获取所有会话
        Collection<Session> sessions = sessionMap.values();
        // 遍历所有会话并发送消息
        for (Session session : sessions) {
            // 检查会话是否已打开
            if (session.isOpen()) {
                // 发送文本消息
                session.getBasicRemote().sendText(message);
            }
        }
    }
}
