package com.campus.trade.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.campus.trade.constant.MessageConstant;
import com.campus.trade.context.BaseContext;
import com.campus.trade.dto.*;
import com.campus.trade.entity.*;
import com.campus.trade.exception.OrderBusinessException;
import com.campus.trade.mapper.*;
import com.campus.trade.result.PageResult;
import com.campus.trade.service.TradeOrderService;
import com.campus.trade.vo.*;
import com.campus.trade.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class TradeOrderServiceImpl implements TradeOrderService {

    @Autowired
    private TradeOrderMapper tradeOrderMapper;

    @Autowired
    private TradeOrderDetailMapper tradeOrderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private ItemMapper itemMapper;

    /**
     * 用户下单
     */
    @Override
    @Transactional
    public TradeOrderSubmitVO submitOrder(TradeOrderSubmitDTO tradeOrderSubmitDTO) {
        log.info("开始处理用户下单请求，参数：{}", tradeOrderSubmitDTO);

        Long userId = BaseContext.getCurrentId();

        // 根据地址ID查询地址信息
        AddressBook addressBook = addressBookMapper.getById(tradeOrderSubmitDTO.getDeliveryAddressId());
        if (addressBook == null) {
            throw new OrderBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        // 检查购物车
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> cartList = shoppingCartMapper.list(shoppingCart);

        if (cartList == null || cartList.isEmpty()) {
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 查询商品信息获取卖家ID
        Long sellerId = null;
        Item item = itemMapper.getById(tradeOrderSubmitDTO.getItemId());
        if (item != null) {
            sellerId = item.getSellerId();
        }

        // 创建交易订单
        TradeOrder tradeOrder = new TradeOrder();
        BeanUtils.copyProperties(tradeOrderSubmitDTO, tradeOrder);
        tradeOrder.setBuyerId(userId);
        tradeOrder.setSellerId(sellerId);
        tradeOrder.setTradeNo(UUID.randomUUID().toString().replace("-", ""));
        tradeOrder.setTradeStatus(TradeOrder.PENDING_PAYMENT);
        tradeOrder.setTradeTime(LocalDateTime.now());
        tradeOrder.setPhone(addressBook.getPhone());
        tradeOrder.setAddress(addressBook.getDetail());
        tradeOrder.setConsignee(addressBook.getConsignee());

        tradeOrderMapper.insert(tradeOrder);

        // 创建订单详情
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : cartList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(tradeOrder.getId());
            orderDetail.setName(cart.getName());
            orderDetail.setItemId(cart.getItemId());
            orderDetail.setBundleId(cart.getBundleId());
            orderDetail.setNumber(cart.getNumber());
            orderDetail.setAmount(cart.getAmount());
            orderDetail.setImage(cart.getImage());
            orderDetails.add(orderDetail);
        }

        tradeOrderDetailMapper.insertBatch(orderDetails);

        // 清空购物车
        shoppingCartMapper.deleteByUserId(userId);

        TradeOrderSubmitVO tradeOrderSubmitVO = TradeOrderSubmitVO.builder()
                .id(tradeOrder.getId())
                .tradeNo(tradeOrder.getTradeNo())
                .totalAmount(tradeOrder.getTotalAmount())
                .tradeTime(tradeOrder.getTradeTime())
                .build();

        log.info("下单成功，交易编号：{}", tradeOrder.getTradeNo());
        return tradeOrderSubmitVO;
    }

    /**
     * 订单支付（模拟支付）
     */
    @Override
    public TradeOrderPaymentVO payment(TradeOrderPaymentDTO tradeOrderPaymentDTO) throws Exception {
        log.info("开始处理订单支付（模拟），交易编号：{}", tradeOrderPaymentDTO.getTradeNo());

        TradeOrder tradeOrder = tradeOrderMapper.getByNumber(tradeOrderPaymentDTO.getTradeNo());

        if (tradeOrder == null) {
            throw new OrderBusinessException(MessageConstant.TRADE_ORDER_NOT_FOUND);
        }

        // 如果订单状态不是待付款，检查是否已处理
        if (tradeOrder.getTradeStatus().equals(TradeOrder.PENDING_SHIPMENT) ||
                tradeOrder.getTradeStatus().equals(TradeOrder.SHIPPED) ||
                tradeOrder.getTradeStatus().equals(TradeOrder.COMPLETED)) {
            log.warn("订单已支付或已处理，直接返回成功，交易编号：{}", tradeOrderPaymentDTO.getTradeNo());
            return TradeOrderPaymentVO.builder()
                    .nonceStr("mock_nonce_str")
                    .paySign("mock_pay_sign")
                    .timeStamp(String.valueOf(System.currentTimeMillis()))
                    .signType("RSA")
                    .packageStr("prepay_id=mock_prepay_id")
                    .build();
        }

        // 只有待付款状态的订单才能支付
        if (!tradeOrder.getTradeStatus().equals(TradeOrder.PENDING_PAYMENT)) {
            throw new OrderBusinessException(MessageConstant.TRADE_STATUS_ERROR);
        }

        // 设置支付方式
        TradeOrder updateOrder = new TradeOrder();
        updateOrder.setId(tradeOrder.getId());
        updateOrder.setPaymentMethod(tradeOrderPaymentDTO.getPaymentMethod());
        tradeOrderMapper.update(updateOrder);

        paySuccess(tradeOrder.getTradeNo());

        TradeOrderPaymentVO tradeOrderPaymentVO = TradeOrderPaymentVO.builder()
                .nonceStr("mock_nonce_str")
                .paySign("mock_pay_sign")
                .timeStamp(String.valueOf(System.currentTimeMillis()))
                .signType("RSA")
                .packageStr("prepay_id=mock_prepay_id")
                .build();

        log.info("模拟支付成功，交易编号：{}", tradeOrder.getTradeNo());
        return tradeOrderPaymentVO;
    }

    /**
     * 支付成功回调处理
     */
    @Override
    @Transactional
    public void paySuccess(String tradeNo) {
        log.info("处理支付成功回调，交易编号：{}", tradeNo);

        TradeOrder tradeOrder = tradeOrderMapper.getByNumber(tradeNo);

        if (tradeOrder == null) {
            log.error("支付回调处理失败，订单不存在，交易编号：{}", tradeNo);
            return;
        }

        // 幂等性处理：已处理的订单不再重复处理
        if (tradeOrder.getTradeStatus().equals(TradeOrder.PENDING_SHIPMENT) ||
                tradeOrder.getTradeStatus().equals(TradeOrder.SHIPPED) ||
                tradeOrder.getTradeStatus().equals(TradeOrder.COMPLETED)) {
            log.warn("订单已处理，无需重复处理，交易编号：{}", tradeNo);
            return;
        }

        TradeOrder updateOrder = new TradeOrder();
        updateOrder.setId(tradeOrder.getId());
        updateOrder.setTradeStatus(TradeOrder.PENDING_SHIPMENT);
        updateOrder.setPaymentTime(LocalDateTime.now());
        tradeOrderMapper.update(updateOrder);

        // 来单提醒：向管理端推送新订单通知
        try {
            Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("type", 1);
            msgMap.put("orderId", tradeOrder.getId());
            msgMap.put("content", "您有新的交易订单");
            String msg = JSON.toJSONString(msgMap);
            WebSocketServer.broadcast(msg);
        } catch (Exception e) {
            log.error("推送来单提醒失败", e);
        }

        log.info("支付成功处理完成，交易编号：{}", tradeNo);
    }

    /**
     * 查询历史订单
     */
    @Override
    public PageResult pageQuery(TradeOrderPageQueryDTO tradeOrderPageQueryDTO) {
        Long userId = BaseContext.getCurrentId();
        tradeOrderPageQueryDTO.setBuyerId(userId);

        PageHelper.startPage(tradeOrderPageQueryDTO.getPage(), tradeOrderPageQueryDTO.getPageSize());
        List<TradeOrder> list = tradeOrderMapper.pageQuery(tradeOrderPageQueryDTO);

        long total = list instanceof Page ? ((Page<TradeOrder>) list).getTotal() : list.size();
        List<TradeOrderVO> voList = new ArrayList<>();

        if (list != null && !list.isEmpty()) {
            for (TradeOrder tradeOrder : list) {
                Long orderId = tradeOrder.getId();
                List<OrderDetail> orderDetails = tradeOrderDetailMapper.getByOrderId(orderId);

                TradeOrderVO tradeOrderVO = new TradeOrderVO();
                BeanUtils.copyProperties(tradeOrder, tradeOrderVO);
                tradeOrderVO.setOrderDetailList(orderDetails);
                voList.add(tradeOrderVO);
            }
        }

        return new PageResult(total, voList);
    }

    /**
     * 查询订单详情
     */
    @Override
    public TradeOrderVO getOrderDetail(Long id) {
        TradeOrder tradeOrder = tradeOrderMapper.getById(id);
        if (tradeOrder == null) {
            throw new OrderBusinessException(MessageConstant.TRADE_ORDER_NOT_FOUND);
        }

        List<OrderDetail> orderDetails = tradeOrderDetailMapper.getByOrderId(id);

        TradeOrderVO tradeOrderVO = new TradeOrderVO();
        BeanUtils.copyProperties(tradeOrder, tradeOrderVO);
        tradeOrderVO.setOrderDetailList(orderDetails);

        return tradeOrderVO;
    }

    /**
     * 取消订单
     */
    @Override
    @Transactional
    public void cancelOrder(Long id) {
        TradeOrder tradeOrder = tradeOrderMapper.getById(id);
        if (tradeOrder == null) {
            throw new OrderBusinessException(MessageConstant.TRADE_ORDER_NOT_FOUND);
        }

        // 只有待付款状态的订单可以取消
        if (!tradeOrder.getTradeStatus().equals(TradeOrder.PENDING_PAYMENT)) {
            throw new OrderBusinessException(MessageConstant.TRADE_STATUS_ERROR);
        }

        TradeOrder updateOrder = new TradeOrder();
        updateOrder.setId(id);
        updateOrder.setTradeStatus(TradeOrder.CANCELLED);
        updateOrder.setCancelTime(LocalDateTime.now());
        tradeOrderMapper.update(updateOrder);

        log.info("订单已取消，id：{}", id);
    }

    /**
     * 订单条件搜索
     */
    @Override
    public PageResult conditionSearch(TradeOrderPageQueryDTO tradeOrderPageQueryDTO) {
        PageHelper.startPage(tradeOrderPageQueryDTO.getPage(), tradeOrderPageQueryDTO.getPageSize());
        List<TradeOrder> list = tradeOrderMapper.pageQuery(tradeOrderPageQueryDTO);

        long total = list instanceof Page ? ((Page<TradeOrder>) list).getTotal() : list.size();
        List<TradeOrderVO> voList = new ArrayList<>();

        if (list != null && !list.isEmpty()) {
            for (TradeOrder tradeOrder : list) {
                Long orderId = tradeOrder.getId();
                List<OrderDetail> orderDetails = tradeOrderDetailMapper.getByOrderId(orderId);

                TradeOrderVO tradeOrderVO = new TradeOrderVO();
                BeanUtils.copyProperties(tradeOrder, tradeOrderVO);
                tradeOrderVO.setOrderDetailList(orderDetails);
                voList.add(tradeOrderVO);
            }
        }

        return new PageResult(total, voList);
    }

    /**
     * 各个状态的订单数量统计
     */
    @Override
    public TradeOrderStatisticsVO statistics() {
        Integer pendingShipment = tradeOrderMapper.countByStatus(TradeOrder.PENDING_SHIPMENT);
        Integer shipped = tradeOrderMapper.countByStatus(TradeOrder.SHIPPED);
        Integer refunding = tradeOrderMapper.countByStatus(TradeOrder.REFUNDING);

        TradeOrderStatisticsVO statisticsVO = new TradeOrderStatisticsVO();
        statisticsVO.setPendingShipment(pendingShipment != null ? pendingShipment : 0);
        statisticsVO.setShipped(shipped != null ? shipped : 0);
        statisticsVO.setRefunding(refunding != null ? refunding : 0);

        return statisticsVO;
    }

    /**
     * 卖家确认订单
     */
    @Override
    @Transactional
    public void confirm(TradeOrderConfirmDTO tradeOrderConfirmDTO) {
        TradeOrder tradeOrder = tradeOrderMapper.getById(tradeOrderConfirmDTO.getId());
        if (tradeOrder == null) {
            throw new OrderBusinessException(MessageConstant.TRADE_ORDER_NOT_FOUND);
        }

        if (!tradeOrder.getTradeStatus().equals(TradeOrder.PENDING_PAYMENT)) {
            throw new OrderBusinessException(MessageConstant.TRADE_STATUS_ERROR);
        }

        TradeOrder updateOrder = new TradeOrder();
        updateOrder.setId(tradeOrderConfirmDTO.getId());
        updateOrder.setTradeStatus(TradeOrder.PENDING_SHIPMENT);
        tradeOrderMapper.update(updateOrder);

        log.info("卖家确认订单，id：{}", tradeOrderConfirmDTO.getId());
    }

    /**
     * 卖家拒单
     */
    @Override
    @Transactional
    public void rejection(TradeOrderRejectionDTO tradeOrderRejectionDTO) {
        TradeOrder tradeOrder = tradeOrderMapper.getById(tradeOrderRejectionDTO.getId());
        if (tradeOrder == null) {
            throw new OrderBusinessException(MessageConstant.TRADE_ORDER_NOT_FOUND);
        }

        if (!tradeOrder.getTradeStatus().equals(TradeOrder.PENDING_PAYMENT)) {
            throw new OrderBusinessException(MessageConstant.TRADE_STATUS_ERROR);
        }

        TradeOrder updateOrder = new TradeOrder();
        updateOrder.setId(tradeOrderRejectionDTO.getId());
        updateOrder.setTradeStatus(TradeOrder.CANCELLED);
        updateOrder.setRejectionReason(tradeOrderRejectionDTO.getRejectionReason());
        updateOrder.setCancelTime(LocalDateTime.now());
        tradeOrderMapper.update(updateOrder);

        log.info("卖家拒单，id：{}，原因：{}", tradeOrderRejectionDTO.getId(), tradeOrderRejectionDTO.getRejectionReason());
    }

    /**
     * 取消订单（商家或用户）
     */
    @Override
    @Transactional
    public void cancel(TradeOrderCancelDTO tradeOrderCancelDTO) {
        TradeOrder tradeOrder = tradeOrderMapper.getById(tradeOrderCancelDTO.getId());
        if (tradeOrder == null) {
            throw new OrderBusinessException(MessageConstant.TRADE_ORDER_NOT_FOUND);
        }

        TradeOrder updateOrder = new TradeOrder();
        updateOrder.setId(tradeOrderCancelDTO.getId());
        updateOrder.setTradeStatus(TradeOrder.CANCELLED);
        updateOrder.setCancelReason(tradeOrderCancelDTO.getCancelReason());
        updateOrder.setCancelTime(LocalDateTime.now());
        tradeOrderMapper.update(updateOrder);

        log.info("取消订单，id：{}，原因：{}", tradeOrderCancelDTO.getId(), tradeOrderCancelDTO.getCancelReason());
    }

    /**
     * 卖家发货
     */
    @Override
    @Transactional
    public void ship(Long id, String trackingNumber) {
        TradeOrder tradeOrder = tradeOrderMapper.getById(id);
        if (tradeOrder == null) {
            throw new OrderBusinessException(MessageConstant.TRADE_ORDER_NOT_FOUND);
        }

        if (!tradeOrder.getTradeStatus().equals(TradeOrder.PENDING_SHIPMENT)) {
            throw new OrderBusinessException(MessageConstant.TRADE_STATUS_ERROR);
        }

        TradeOrder updateOrder = new TradeOrder();
        updateOrder.setId(id);
        updateOrder.setTradeStatus(TradeOrder.SHIPPED);
        if (trackingNumber != null && !trackingNumber.isEmpty()) {
            updateOrder.setTrackingNumber(trackingNumber);
        }
        tradeOrderMapper.update(updateOrder);

        log.info("卖家发货，订单id：{}，物流单号：{}", id, trackingNumber);
    }

    /**
     * 查询卖家已售订单
     */
    @Override
    public PageResult soldOrders(TradeOrderPageQueryDTO tradeOrderPageQueryDTO) {
        Long sellerId = BaseContext.getCurrentId();
        tradeOrderPageQueryDTO.setSellerId(sellerId);

        PageHelper.startPage(tradeOrderPageQueryDTO.getPage(), tradeOrderPageQueryDTO.getPageSize());
        List<TradeOrder> list = tradeOrderMapper.pageQuery(tradeOrderPageQueryDTO);

        long total = list instanceof Page ? ((Page<TradeOrder>) list).getTotal() : list.size();
        List<TradeOrderVO> voList = new ArrayList<>();

        if (list != null && !list.isEmpty()) {
            for (TradeOrder tradeOrder : list) {
                List<OrderDetail> orderDetails = tradeOrderDetailMapper.getByOrderId(tradeOrder.getId());

                TradeOrderVO tradeOrderVO = new TradeOrderVO();
                BeanUtils.copyProperties(tradeOrder, tradeOrderVO);
                tradeOrderVO.setOrderDetailList(orderDetails);
                voList.add(tradeOrderVO);
            }
        }

        return new PageResult(total, voList);
    }

    /**
     * 买家确认收货（完成订单）
     */
    @Override
    @Transactional
    public void complete(Long id) {
        TradeOrder tradeOrder = tradeOrderMapper.getById(id);
        if (tradeOrder == null) {
            throw new OrderBusinessException(MessageConstant.TRADE_ORDER_NOT_FOUND);
        }

        if (!tradeOrder.getTradeStatus().equals(TradeOrder.SHIPPED)) {
            throw new OrderBusinessException(MessageConstant.TRADE_STATUS_ERROR);
        }

        TradeOrder updateOrder = new TradeOrder();
        updateOrder.setId(id);
        updateOrder.setTradeStatus(TradeOrder.COMPLETED);
        updateOrder.setConfirmTime(LocalDateTime.now());
        tradeOrderMapper.update(updateOrder);

        log.info("买家确认收货，订单id：{}", id);
    }

    /**
     * 用户催单
     */
    @Override
    public void reminder(Long id) {
        TradeOrder tradeOrder = tradeOrderMapper.getById(id);
        if (tradeOrder == null) {
            throw new OrderBusinessException(MessageConstant.TRADE_ORDER_NOT_FOUND);
        }

        // 通过WebSocket推送催单提醒
        try {
            Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("type", 2);
            msgMap.put("orderId", id);
            msgMap.put("content", "买家催一下发货");
            String msg = JSON.toJSONString(msgMap);
            WebSocketServer.broadcast(msg);
            log.info("催单提醒已推送，交易编号：{}", tradeOrder.getTradeNo());
        } catch (Exception e) {
            log.error("推送催单提醒失败", e);
        }
    }
}
