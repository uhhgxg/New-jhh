package com.campus.trade.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * AI聊天请求的数据传输对象(DTO)
 * 使用@Data注解自动生成getter、setter等方法
 * 使用@ApiModel注解标记为Swagger API模型，用于API文档生成
 */
@Data
@ApiModel(description = "AI聊天请求")
public class AiChatDTO {

    /**
     * 用户发送的消息内容
     * 使用@ApiModelProperty注解描述API文档属性
     * value属性提供字段说明
     * required属性标记该字段为必填项
     */
    @ApiModelProperty(value = "用户消息", required = true)
    private String message;

    /**
     * 会话ID
     * 用于标识聊天会话，可选参数
     * 如果不提供，系统会自动创建新的会话
     */
    @ApiModelProperty("会话ID（可选，不传则自动创建新会话）")
    private String conversationId;

    /**
     * 调用模式
     * 支持两种模式：
     * - chat: 默认聊天模式
     * - vector: 向量检索模式
     */
    @ApiModelProperty("调用模式: chat(默认)/vector(向量检索)")
    private String mode;
}
