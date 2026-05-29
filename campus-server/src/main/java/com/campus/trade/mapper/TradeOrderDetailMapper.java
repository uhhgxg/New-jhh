package com.campus.trade.mapper;

import com.campus.trade.dto.ItemSalesDTO;
import com.campus.trade.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TradeOrderDetailMapper {

    void insertBatch(@Param("orderDetailList") List<OrderDetail> orderDetailList);

    List<OrderDetail> getByOrderId(Long orderId);

    List<ItemSalesDTO> getSalesTop10(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);
}
