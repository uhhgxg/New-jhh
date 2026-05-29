package com.campus.trade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "AI聊天响应")
public class AiChatVO {

    @ApiModelProperty("AI回复内容 / 向量检索结果文本")
    private String reply;

    @ApiModelProperty("会话ID")
    private String conversationId;

    @ApiModelProperty("使用的模型")
    private String model;

    @ApiModelProperty("消耗的token数")
    private Integer tokensUsed;

    @ApiModelProperty("向量检索匹配项（仅 vector 模式）")
    private List<VectorMatchItem> matches;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(description = "向量匹配项")
    public static class VectorMatchItem {

        @ApiModelProperty("名称")
        private String name;

        @ApiModelProperty("价格")
        private String price;

        @ApiModelProperty("分类")
        private String category;

        @ApiModelProperty("描述")
        private String description;

        @ApiModelProperty("相似度分数")
        private double score;

        @ApiModelProperty("类型: item / bundle")
        private String type;
    }
}
