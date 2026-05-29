package com.campus.trade.controller.admin;

import com.campus.trade.result.Result;
import com.campus.trade.service.WorkspaceService;
import com.campus.trade.vo.BusinessDataVO;
import com.campus.trade.vo.BundleOverViewVO;
import com.campus.trade.vo.ItemOverViewVO;
import com.campus.trade.vo.TradeOrderOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 工作台控制器
 * 提供工作台相关的数据查询接口
 */
@Slf4j
@RestController("adminWorkSpaceController")
@RequestMapping("/admin/workspace")
@Api(tags = "工作台相关接口")
public class WorkSpaceController {

    /**
     * 工作台服务接口
     * 用于处理工作台相关的业务逻辑
     */
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 查询工作台今日数据
     * @return 返回包含今日业务数据的BusinessDataVO对象
     */
    @GetMapping("/businessData")
    @ApiOperation("工作台今日数据查询")
    public Result<BusinessDataVO> businessData() {
        // 获取今天的开始时间（0点）
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        // 获取今天的结束时间（23:59:59）
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);
        // 调用服务层获取业务数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(begin, end);
        // 返回成功响应，包含业务数据
        return Result.success(businessDataVO);
    }

    /**
     * 查询订单管理数据
     * @return 返回包含订单管理数据的TradeOrderOverViewVO对象
     */
    @GetMapping("/overviewOrders")
    @ApiOperation("查询订单管理数据")
    public Result<TradeOrderOverViewVO> orderOverView() {
        // 调用服务层获取订单管理数据并返回
        return Result.success(workspaceService.getOrderOverView());
    }

    /**
     * 查询商品总览数据
     * @return 返回包含商品总览数据的ItemOverViewVO对象
     */
    @GetMapping("/overviewItems")
    @ApiOperation("查询商品总览")
    public Result<ItemOverViewVO> itemOverView() {
        // 调用服务层获取商品总览数据并返回
        return Result.success(workspaceService.getItemOverView());
    }

    /**
     * 查询捆绑包总览数据
     * @return 返回包含捆绑包总览数据的BundleOverViewVO对象
     */
    @GetMapping("/overviewBundles")
    @ApiOperation("查询捆绑包总览")
    public Result<BundleOverViewVO> bundleOverView() {
        // 调用服务层获取捆绑包总览数据并返回
        return Result.success(workspaceService.getBundleOverView());
    }
}
