package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component          // 将此类标记为Spring组件，由Spring容器管理
@Slf4j              // 使用Lombok的@Slf4j注解，自动生成日志记录器
public class OrderTask {    // 订单任务类，用于处理订单相关的定时任务

    @Autowired      // 自动注入OrdersMapper，用于数据库操作
    private OrdersMapper ordersMapper;

    /**
     * 每分钟检查一次支付超时订单
     * 下单后超过15分钟仍未支付则自动取消
     * 这是一个使用Spring @Scheduled注解的定时任务方法
     * 当系统时间到达每分钟的整点时，此方法会自动执行
     */
    @Scheduled(cron = "0 0/1 * * * ?")  // cron表达式，表示每分钟执行一次
    public void processTimeoutOrder() {   // 处理支付超时订单的方法
        log.info("=== 定时任务：处理支付超时订单 ===");  // 记录任务开始执行的日志

        // 计算15分钟前的时间点，作为支付超时的截止时间
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(15);
        // 查询所有状态为"待支付"且下单时间早于截止时间的订单
        List<Orders> orders = ordersMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, deadline);

        // 检查订单列表是否为空
        if (orders == null || orders.isEmpty()) {
            log.info("暂无支付超时订单");  // 记录无订单的日志信息
            return;  // 如果没有订单，则直接结束方法
        }

        // 遍历所有超时订单
        for (Orders order : orders) {
            // 设置订单状态为已取消
            order.setStatus(Orders.CANCELLED);
            // 设置取消原因为支付超时，系统自动取消
            order.setCancelReason("支付超时，系统自动取消");
            // 设置取消时间为当前时间
            order.setCancelTime(LocalDateTime.now());
            // 更新数据库中的订单信息
            ordersMapper.update(order);
            // 记录已取消的订单号
            log.info("已自动取消订单：{}", order.getNumber());
        }
    }

    /**
     * 每天凌晨1点检查并自动完成派送中的订单
 * 这是一个使用Spring @Scheduled注解的定时任务方法
 * 当系统时间到达每天凌晨1点时，此方法会自动执行
     */
    @Scheduled(cron = "0 0 1 * * ?")  // cron表达式，表示每天凌晨1点执行
    public void processDeliveryOrder() {  // 处理派送中订单的方法
        log.info("=== 定时任务：自动完成派送中订单 ===");  // 记录任务开始执行的日志

    // 从数据库获取所有状态为"派送中"的订单列表
        List<Orders> orders = ordersMapper.getByStatus(Orders.DELIVERY_IN_PROGRESS);

    // 检查订单列表是否为空
        if (orders == null || orders.isEmpty()) {
            log.info("暂无派送中的订单");  // 记录无订单的日志信息
            return;  // 如果没有订单，则直接结束方法
        }

    // 遍历所有派送中的订单
        for (Orders order : orders) {
        // 将订单状态更新为已完成
            order.setStatus(Orders.COMPLETED);
        // 更新数据库中的订单信息
            ordersMapper.update(order);
        // 记录已完成的订单号
            log.info("已自动完成订单：{}", order.getNumber());
        }
    }
}
