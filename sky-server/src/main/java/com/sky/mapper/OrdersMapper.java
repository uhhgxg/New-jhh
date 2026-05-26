
package com.sky.mapper;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrdersMapper {

    /**
     * 新增订单
     *
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据用户ID查询订单
     *
     * @param userId
     * @return
     */
    List<Orders> getByUserId(Long userId);

    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     * @return
     */
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);

    /**
     * 根据ID查询订单
     *
     * @param id
     * @return
     */
    Orders getById(Long id);

    /**
     * 分页条件查询订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    List<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据状态统计订单数量
     *
     * @param status
     * @return
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countByStatus(Integer status);

    /**
     * 根据动态条件统计订单数量
     *
     * @param map
     * @return
     */
    Integer countByMap(Map<String, Object> map);

    /**
     * 根据动态条件统计营业额（已完成订单金额总和）
     *
     * @param map
     * @return
     */
    BigDecimal sumByMap(Map<String, Object> map);

    /**
     * 根据状态和下单时间查询订单
     *
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(@Param("status") Integer status, @Param("orderTime") LocalDateTime orderTime);

    /**
     * 根据状态查询订单
     *
     * @param status
     * @return
     */
    @Select("select * from orders where status = #{status}")
    List<Orders> getByStatus(Integer status);
}
