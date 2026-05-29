package com.campus.trade.mapper;

import com.campus.trade.dto.TradeOrderPageQueryDTO;
import com.campus.trade.entity.TradeOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface TradeOrderMapper {

    void insert(TradeOrder tradeOrder);

    List<TradeOrder> getByUserId(Long userId);

    TradeOrder getByNumber(String tradeNo);

    void update(TradeOrder tradeOrder);

    TradeOrder getById(Long id);

    List<TradeOrder> pageQuery(TradeOrderPageQueryDTO tradeOrderPageQueryDTO);

    @Select("select count(id) from trade_order where trade_status = #{tradeStatus}")
    Integer countByStatus(Integer tradeStatus);

    Integer countByMap(Map<String, Object> map);

    BigDecimal sumByMap(Map<String, Object> map);

    @Select("select * from trade_order where trade_status = #{tradeStatus} and trade_time < #{tradeTime}")
    List<TradeOrder> getByStatusAndTradeTimeLT(@Param("tradeStatus") Integer tradeStatus, @Param("tradeTime") LocalDateTime tradeTime);

    @Select("select * from trade_order where trade_status = #{tradeStatus} and payment_time < #{paymentTime}")
    List<TradeOrder> getByStatusAndPaymentTimeLT(@Param("tradeStatus") Integer tradeStatus, @Param("paymentTime") LocalDateTime paymentTime);

    @Select("select * from trade_order where trade_status = #{tradeStatus}")
    List<TradeOrder> getByStatus(Integer tradeStatus);
}
