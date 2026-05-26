
package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 批量插入订单明细
     *
     * @param orderDetailList
     */
    void insertBatch(@Param("orderDetailList") List<OrderDetail> orderDetailList);

    /**
     * 根据订单ID查询订单明细
     *
     * @param orderId
     * @return
     */
    List<OrderDetail> getByOrderId(Long orderId);

    /**
     * 查询销量排名top10（按时间区间内已完成订单的商品销量降序排列）
     *
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);
}
