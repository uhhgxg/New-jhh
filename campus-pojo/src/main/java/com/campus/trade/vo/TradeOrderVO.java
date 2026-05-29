package com.campus.trade.vo;

import com.campus.trade.entity.OrderDetail;
import com.campus.trade.entity.TradeOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

/**
 * 交易订单视图对象(VO)
 * 继承自TradeOrder类并实现Serializable接口，用于展示交易订单的相关信息
 */
@Data                   // 使用Lombok注解自动生成getter、setter等方法
@NoArgsConstructor      // 使用Lombok注解自动生成无参构造方法
@AllArgsConstructor     // 使用Lombok注解自动生成全参构造方法
public class TradeOrderVO extends TradeOrder implements Serializable {

    //订单商品信息，以字符串形式存储商品数据
    private String orderItems;

    //订单详情列表，存储订单的详细商品信息
    private List<OrderDetail> orderDetailList;
}
