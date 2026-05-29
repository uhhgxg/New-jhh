package com.campus.trade.controller.admin;

import com.campus.trade.dto.TradeOrderCancelDTO;
import com.campus.trade.dto.TradeOrderConfirmDTO;
import com.campus.trade.dto.TradeOrderPageQueryDTO;
import com.campus.trade.dto.TradeOrderRejectionDTO;
import com.campus.trade.result.PageResult;
import com.campus.trade.result.Result;
import com.campus.trade.service.TradeOrderService;
import com.campus.trade.vo.TradeOrderStatisticsVO;
import com.campus.trade.vo.TradeOrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * B端-交易订单管理控制器
 * 提供订单搜索、统计、详情查询、确认交易、拒绝交易、取消订单、发货和完成订单等功能
 */
@Slf4j
@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Api(tags = "B端-交易订单管理接口")
public class OrderController {

    @Autowired
    private TradeOrderService tradeOrderService; // 注入交易订单服务

    /**
     * 订单搜索
     * @param tradeOrderPageQueryDTO 订单分页查询条件
     * @return 返回分页查询结果
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult> conditionSearch(TradeOrderPageQueryDTO tradeOrderPageQueryDTO) {
        log.info("订单搜索：{}", tradeOrderPageQueryDTO);
        PageResult pageResult = tradeOrderService.conditionSearch(tradeOrderPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<TradeOrderStatisticsVO> statistics() {
        log.info("各个状态的订单数量统计");
        TradeOrderStatisticsVO tradeOrderStatisticsVO = tradeOrderService.statistics();
        return Result.success(tradeOrderStatisticsVO);
    }

    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<TradeOrderVO> details(@PathVariable Long id) {
        log.info("查询订单详情：{}", id);
        TradeOrderVO tradeOrderVO = tradeOrderService.getOrderDetail(id);
        return Result.success(tradeOrderVO);
    }

    @PutMapping("/confirm")
    @ApiOperation("确认交易")
    public Result confirm(@RequestBody TradeOrderConfirmDTO tradeOrderConfirmDTO) {
        log.info("确认交易：{}", tradeOrderConfirmDTO);
        tradeOrderService.confirm(tradeOrderConfirmDTO);
        return Result.success();
    }

    @PutMapping("/rejection")
    @ApiOperation("拒绝交易")
    public Result rejection(@RequestBody TradeOrderRejectionDTO tradeOrderRejectionDTO) {
        log.info("拒绝交易：{}", tradeOrderRejectionDTO);
        tradeOrderService.rejection(tradeOrderRejectionDTO);
        return Result.success();
    }

    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody TradeOrderCancelDTO tradeOrderCancelDTO) {
        log.info("取消订单：{}", tradeOrderCancelDTO);
        tradeOrderService.cancel(tradeOrderCancelDTO);
        return Result.success();
    }

    @PutMapping("/ship/{id}")
    @ApiOperation("发货")
    public Result ship(@PathVariable Long id) {
        log.info("发货：{}", id);
        tradeOrderService.ship(id, null);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable Long id) {
        log.info("完成订单：{}", id);
        tradeOrderService.complete(id);
        return Result.success();
    }
}
