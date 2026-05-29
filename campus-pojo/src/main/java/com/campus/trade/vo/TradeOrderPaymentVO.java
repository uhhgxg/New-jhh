package com.campus.trade.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 交易订单支付值对象(VO)
 * 用于封装支付相关的数据信息
 * 实现了Serializable接口以支持序列化
 * 使用了Lombok注解简化代码
 * @Data: 自动生成getter、setter、toString等方法
 * @Builder: 提供构建器模式创建对象
 * @NoArgsConstructor: 生成无参构造方法
 * @AllArgsConstructor: 生成全参构造方法
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeOrderPaymentVO implements Serializable {

    private String nonceStr; //随机字符串，用于防止重复提交
    private String paySign; //签名
    private String timeStamp; //时间戳
    private String signType; //签名算法
    private String packageStr; //统一下单接口返回的 prepay_id 参数值
}
