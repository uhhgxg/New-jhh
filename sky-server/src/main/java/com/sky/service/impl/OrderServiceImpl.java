package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.BaiduMapService;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private BaiduMapService baiduMapService;

    /**
     * 用户下单
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        log.info("开始处理用户下单请求，参数：{}", ordersSubmitDTO);

        Long userId = BaseContext.getCurrentId();

        // 根据地址ID查询地址信息
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new RuntimeException("地址信息不存在");
        }

        // 校验配送距离是否超出范围
        boolean inRange = baiduMapService.isWithinDeliveryRange(addressBook);
        if (!inRange) {
            throw new OrderBusinessException("您的地址超出配送范围，无法下单");
        }

        // 检查购物车
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> cartList = shoppingCartMapper.list(shoppingCart);

        if (cartList == null || cartList.size() == 0) {
            throw new RuntimeException("购物车为空，无法下单");
        }

        // 创建订单
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, order);
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPayStatus(Orders.UN_PAID);
        order.setUserId(userId);
        order.setOrderTime(LocalDateTime.now());
        order.setCheckoutTime(LocalDateTime.now());

        ordersMapper.insert(order);

        // 创建订单详情
        List<OrderDetail> orderDetails = new ArrayList<>();
        cartList.forEach(cart -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(order.getId());
            orderDetail.setName(cart.getName());
            orderDetail.setDishId(cart.getDishId());
            orderDetail.setSetmealId(cart.getSetmealId());
            orderDetail.setDishFlavor(cart.getDishFlavor());
            orderDetail.setNumber(cart.getNumber());
            orderDetail.setAmount(cart.getAmount());
            orderDetail.setImage(cart.getImage());
            orderDetails.add(orderDetail);
        });

        orderDetailMapper.insertBatch(orderDetails);

        // 清空购物车
        shoppingCartMapper.deleteByUserId(userId);

        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();

        log.info("下单成功，订单号：{}", order.getNumber());
        return orderSubmitVO;
    }

    /**
     * 订单支付（模拟支付）
     * 处理用户支付请求，支持幂等性处理（已支付订单直接返回成功）
     *
     * @param ordersPaymentDTO 支付请求数据传输对象
     * @return 订单支付结果视图对象
     * @throws Exception 支付过程中可能出现的异常
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("开始处理订单支付（模拟），订单号：{}", ordersPaymentDTO.getOrderNumber());

        Orders order = ordersMapper.getByNumber(ordersPaymentDTO.getOrderNumber());

        if (order == null) {
            throw new OrderBusinessException("订单不存在");
        }

        // 如果订单已支付，直接返回成功（幂等性处理）
        if (order.getPayStatus() == Orders.PAID) {
            log.warn("订单已支付，直接返回成功，订单号：{}", ordersPaymentDTO.getOrderNumber());
            return OrderPaymentVO.builder()
                    .nonceStr("mock_nonce_str")
                    .paySign("mock_pay_sign")
                    .timeStamp(String.valueOf(System.currentTimeMillis()))
                    .signType("RSA")
                    .packageStr("prepay_id=mock_prepay_id")
                    .build();
        }

        // 只有待付款状态的订单才能支付
        if (order.getStatus() != Orders.PENDING_PAYMENT) {
            throw new OrderBusinessException(
                String.format("订单状态不正确，无法支付（当前状态：%s）", getOrderStatusText(order.getStatus()))
            );
        }

        paySuccess(order.getNumber());

        OrderPaymentVO orderPaymentVO = OrderPaymentVO.builder()
                .nonceStr("mock_nonce_str")
                .paySign("mock_pay_sign")
                .timeStamp(String.valueOf(System.currentTimeMillis()))
                .signType("RSA")
                .packageStr("prepay_id=mock_prepay_id")
                .build();

        log.info("模拟支付成功，订单号：{}", order.getNumber());
        return orderPaymentVO;
    }

    /**
     * 支付成功回调处理
     */
    @Transactional
    public void paySuccess(String orderNumber) {
        log.info("处理支付成功回调，订单号：{}", orderNumber);

        Orders order = ordersMapper.getByNumber(orderNumber);

        if (order == null) {
            log.error("支付回调处理失败，订单不存在，订单号：{}", orderNumber);
            return;
        }

        if (order.getPayStatus() == Orders.PAID) {
            log.warn("订单已支付，无需重复处理，订单号：{}", orderNumber);
            return;
        }

        Orders updateOrder = new Orders();
        updateOrder.setId(order.getId());
        updateOrder.setPayStatus(Orders.PAID);
        updateOrder.setStatus(Orders.TO_BE_CONFIRMED);
        updateOrder.setCheckoutTime(LocalDateTime.now());

        ordersMapper.update(updateOrder);

        // 来单提醒：向管理端推送新订单通知
        try {
          Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("type", 1);
            msgMap.put("orderId", order.getId());
            msgMap.put("content", "您有新的订单");
            String msg = JSON.toJSONString(msgMap);
            WebSocketServer.broadcast(msg);
        } catch (Exception e) {
            log.error("推送来单提醒失败", e);
        }

        log.info("支付成功处理完成，订单号：{}，订单状态更新为待接单", orderNumber);
    }

    /**
     * 查询历史订单
     */
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());

        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = (Page<Orders>) ordersMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();
        if (page != null && page.size() > 0) {
            for (Orders orders : page) {
                Long orderId = orders.getId();

                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);
                list.add(orderVO);
            }
        }

        return new PageResult(page.getTotal(), list);
    }

    /**
     * 查询订单详情
     */
    public OrderVO getOrderDetail(Long id) {
        Orders orders = ordersMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }

        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);

        return orderVO;
    }

    /**
     * 取消订单
     */
    public void cancelOrder(Long id) {
        Orders orders = ordersMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }

        // 校验订单状态，只有待付款、待接单、已接单、派送中的订单可以取消
        if (orders.getStatus() > Orders.DELIVERY_IN_PROGRESS) {
            throw new OrderBusinessException("订单状态错误，无法取消");
        }

        Orders updateOrder = new Orders();
        updateOrder.setId(id);
        updateOrder.setStatus(Orders.CANCELLED);
        updateOrder.setCancelReason("用户取消订单");
        updateOrder.setCancelTime(LocalDateTime.now());

        // 如果已支付，需要退款
        if (orders.getPayStatus() == Orders.PAID) {
            updateOrder.setPayStatus(Orders.REFUND);
        }

        ordersMapper.update(updateOrder);
    }

    /**
     * 再来一单
     */
    public void repetition(Long id) {
        Long userId = BaseContext.getCurrentId();

        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);

        // 将订单详情中的商品添加到购物车
        List<ShoppingCart> shoppingCartList = orderDetails.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 订单搜索
     */
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = (Page<Orders>) ordersMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();
        if (page != null && page.size() > 0) {
            for (Orders orders : page) {
                Long orderId = orders.getId();

                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);
                list.add(orderVO);
            }
        }

        return new PageResult(page.getTotal(), list);
    }

    /**
     * 各个状态的订单数量统计
     */
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = ordersMapper.countByStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = ordersMapper.countByStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = ordersMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);

        return orderStatisticsVO;
    }

    /**
     * 接单
     */
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = ordersMapper.getById(ordersConfirmDTO.getId());
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }

        if (orders.getStatus() != Orders.TO_BE_CONFIRMED) {
            throw new OrderBusinessException("订单状态错误，无法接单");
        }

        Orders updateOrder = new Orders();
        updateOrder.setId(ordersConfirmDTO.getId());
        updateOrder.setStatus(Orders.CONFIRMED);
        ordersMapper.update(updateOrder);
    }

    /**
     * 拒单
     */
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = ordersMapper.getById(ordersRejectionDTO.getId());
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }

        if (orders.getStatus() != Orders.TO_BE_CONFIRMED) {
            throw new OrderBusinessException("订单状态错误，无法拒单");
        }

        Orders updateOrder = new Orders();
        updateOrder.setId(ordersRejectionDTO.getId());
        updateOrder.setStatus(Orders.CANCELLED);
        updateOrder.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        updateOrder.setCancelTime(LocalDateTime.now());

        // 如果已支付，需要退款
        if (orders.getPayStatus() == Orders.PAID) {
            updateOrder.setPayStatus(Orders.REFUND);
        }

        ordersMapper.update(updateOrder);
    }

    /**
     * 取消订单（商家）
     */
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = ordersMapper.getById(ordersCancelDTO.getId());
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }

        Orders updateOrder = new Orders();
        updateOrder.setId(ordersCancelDTO.getId());
        updateOrder.setStatus(Orders.CANCELLED);
        updateOrder.setCancelReason(ordersCancelDTO.getCancelReason());
        updateOrder.setCancelTime(LocalDateTime.now());

        // 如果已支付，需要退款
        if (orders.getPayStatus() == Orders.PAID) {
            updateOrder.setPayStatus(Orders.REFUND);
        }

        ordersMapper.update(updateOrder);
    }

    /**
     * 派送订单
     */
    public void delivery(Long id) {
        Orders orders = ordersMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }

        if (orders.getStatus() != Orders.CONFIRMED) {
            throw new OrderBusinessException("订单状态错误，无法派送");
        }

        Orders updateOrder = new Orders();
        updateOrder.setId(id);
        updateOrder.setStatus(Orders.DELIVERY_IN_PROGRESS);
        ordersMapper.update(updateOrder);
    }

    /**
     * 完成订单
     */
    public void complete(Long id) {
        Orders orders = ordersMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }

        if (orders.getStatus() != Orders.DELIVERY_IN_PROGRESS) {
            throw new OrderBusinessException("订单状态错误，无法完成");
        }

        Orders updateOrder = new Orders();
        updateOrder.setId(id);
        updateOrder.setStatus(Orders.COMPLETED);
        updateOrder.setDeliveryTime(LocalDateTime.now());
        ordersMapper.update(updateOrder);
    }

    /**
     * 用户催单
     */
    public void reminder(Long id) {
        Orders orders = ordersMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }

        String msg = "{\"type\":2,\"orderId\":" + id + ",\"content\":\"订单催一催\"}";
        try {
            WebSocketServer.broadcast(msg);
            log.info("催单提醒已推送，订单号：{}", orders.getNumber());
        } catch (Exception e) {
            log.error("推送催单提醒失败", e);
        }
    }

    /**
     * 获取订单状态文本描述
     */
    private String getOrderStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        
        if (status.equals(Orders.PENDING_PAYMENT)) {
            return "待付款";
        } else if (status.equals(Orders.TO_BE_CONFIRMED)) {
            return "待接单";
        } else if (status.equals(Orders.CONFIRMED)) {
            return "已接单";
        } else if (status.equals(Orders.DELIVERY_IN_PROGRESS)) {
            return "派送中";
        } else if (status.equals(Orders.COMPLETED)) {
            return "已完成";
        } else if (status.equals(Orders.CANCELLED)) {
            return "已取消";
        } else {
            return "未知状态(" + status + ")";
        }
    }
}
