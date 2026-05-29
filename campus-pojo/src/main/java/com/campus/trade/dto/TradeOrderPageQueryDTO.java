package com.campus.trade.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 交易订单分页查询数据传输对象
 * 用于接收前端传递的交易订单查询条件参数
 */
@Data  // Lombok注解，自动生成getter、setter等方法
public class TradeOrderPageQueryDTO implements Serializable {

    private int page;             // 当前页码

    private int pageSize;         // 每页显示条数

    private String tradeNo;       // 交易订单编号

    private String phone;         // 手机号码

    private Integer tradeStatus;  // 交易状态

    /**
     * 交易开始时间
     * 使用DateTimeFormat注解指定日期时间格式为"yyyy-MM-dd HH:mm:ss"
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    /**
     * 交易结束时间
     * 使用DateTimeFormat注解指定日期时间格式为"yyyy-MM-dd HH:mm:ss"
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Long buyerId;         // 买家ID

    private Long sellerId;        // 卖家ID
}
