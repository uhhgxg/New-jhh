package com.campus.trade.controller.user;

import com.campus.trade.dto.TradeOrderPageQueryDTO;
import com.campus.trade.dto.TradeOrderPaymentDTO;
import com.campus.trade.dto.TradeOrderSubmitDTO;
import com.campus.trade.result.PageResult;
import com.campus.trade.result.Result;
import com.campus.trade.service.TradeOrderService;
import com.campus.trade.vo.TradeOrderPaymentVO;
import com.campus.trade.vo.TradeOrderSubmitVO;
import com.campus.trade.vo.TradeOrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户订单控制器
 * 提供用户订单相关的RESTful API，包括下单、支付、查询、取消等功能
 */
@Slf4j
@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "C端-交易订单接口")
public class OrderController {

    /**
     * 注入交易订单服务
     */
    @Autowired
    private TradeOrderService tradeOrderService;

    /**
     * 用户下单接口
     * @param tradeOrderSubmitDTO 下单请求参数
     * @return 返回下单结果
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<TradeOrderSubmitVO> submit(@RequestBody TradeOrderSubmitDTO tradeOrderSubmitDTO) {
        log.info("用户下单：{}", tradeOrderSubmitDTO);
        TradeOrderSubmitVO tradeOrderSubmitVO = tradeOrderService.submitOrder(tradeOrderSubmitDTO);
        return Result.success(tradeOrderSubmitVO);
    }

    /**
     * 订单支付接口
     * @param tradeOrderPaymentDTO 支付请求参数
     * @return 返回支付结果
     * @throws Exception 支付过程中可能出现的异常
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<TradeOrderPaymentVO> payment(@RequestBody TradeOrderPaymentDTO tradeOrderPaymentDTO) throws Exception {
        log.info("订单支付：{}", tradeOrderPaymentDTO);
        TradeOrderPaymentVO tradeOrderPaymentVO = tradeOrderService.payment(tradeOrderPaymentDTO);
        return Result.success(tradeOrderPaymentVO);
    }

    /**
     * 支付成功回调接口
     * @param orderNumber 订单号
     * @return 返回操作结果
     */
    @GetMapping("/paySuccess/{orderNumber}")
    @ApiOperation("支付成功回调")
    public Result paySuccess(@PathVariable String orderNumber) {
        log.info("支付成功回调：{}", orderNumber);
        tradeOrderService.paySuccess(orderNumber);
        return Result.success();
    }

    /**
     * 查询历史订单接口
     * @param page 页码
     * @param pageSize 每页大小
     * @param status 订单状态（可选）
     * @return 返回分页结果
     */
    @GetMapping("/historyOrders")
    @ApiOperation("查询历史订单")
    public Result<PageResult> historyOrders(@RequestParam int page,
                                            @RequestParam int pageSize,
                                            @RequestParam(required = false) Integer status) {
        log.info("查询历史订单：page={}, pageSize={}, status={}", page, pageSize, status);
        TradeOrderPageQueryDTO tradeOrderPageQueryDTO = new TradeOrderPageQueryDTO();
        tradeOrderPageQueryDTO.setPage(page);
        tradeOrderPageQueryDTO.setPageSize(pageSize);
        tradeOrderPageQueryDTO.setTradeStatus(status);
        PageResult pageResult = tradeOrderService.pageQuery(tradeOrderPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情接口
     * @param id 订单ID
     * @return 返回订单详情
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<TradeOrderVO> orderDetail(@PathVariable Long id) {
        log.info("查询订单详情：{}", id);
        TradeOrderVO tradeOrderVO = tradeOrderService.getOrderDetail(id);
        return Result.success(tradeOrderVO);
    }

    /**
     * 取消订单接口
     * @param id 订单ID
     * @return 返回操作结果
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long id) {
        log.info("取消订单：{}", id);
        tradeOrderService.cancelOrder(id);
        return Result.success();
    }

    /**
     * 买家催发货接口
     * @param id 订单ID
     * @return 返回操作结果
     */
    @GetMapping("/reminder/{id}")
    @ApiOperation("买家催发货")
    public Result reminder(@PathVariable Long id) {
        log.info("买家催发货：{}", id);
        tradeOrderService.reminder(id);
        return Result.success();
    }

    /**
     * 查询卖家订单接口
     * @param page 页码
     * @param pageSize 每页大小
     * @param status 订单状态（可选）
     * @return 返回分页结果
     */
    @GetMapping("/soldOrders")
    @ApiOperation("查询我卖出的订单")
    public Result<PageResult> soldOrders(@RequestParam int page,
                                          @RequestParam int pageSize,
                                          @RequestParam(required = false) Integer status) {
        log.info("查询我卖出的订单：page={}, pageSize={}, status={}", page, pageSize, status);
        TradeOrderPageQueryDTO dto = new TradeOrderPageQueryDTO();
        dto.setPage(page);
        dto.setPageSize(pageSize);
        dto.setTradeStatus(status);
        PageResult pageResult = tradeOrderService.soldOrders(dto);
        return Result.success(pageResult);
    }

    /**
     * 卖家发货接口
     * @param id 订单ID
     * @param trackingNumber 物流单号（可选）
     * @return 返回操作结果
     */
    @PutMapping("/ship/{id}")
    @ApiOperation("卖家发货")
    public Result ship(@PathVariable Long id, @RequestParam(required = false) String trackingNumber) {
        log.info("卖家发货，订单id：{}，物流单号：{}", id, trackingNumber);
        tradeOrderService.ship(id, trackingNumber);
        return Result.success();
    }

    /**
     * 买家确认收货接口
     * @param id 订单ID
     * @return 返回操作结果
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("买家确认收货")
    public Result complete(@PathVariable Long id) {
        log.info("买家确认收货：{}", id);
        tradeOrderService.complete(id);
        return Result.success();
    }
}
