package com.campus.trade.controller.user;

import com.campus.trade.dto.ShoppingCartDTO;
import com.campus.trade.entity.ShoppingCart;
import com.campus.trade.result.Result;
import com.campus.trade.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器类
 * 处理用户购物车相关的HTTP请求，包括添加商品、减少商品数量和查看购物车列表
 */
@Slf4j
@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags = "用户购物车相关接口")
public class ShoppingCartController {
    // 自动注入购物车服务接口实现类
    @Autowired
    private ShoppingCartService shoppingCartService;
    
    /**
     * 添加商品到购物车
     * @param shoppingCartDTO 购物车数据传输对象，包含商品信息
     * @return 返回操作结果
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        // 记录添加购物车的日志信息
        log.info("添加购物车：{}", shoppingCartDTO);
        // 调用服务层方法添加商品到购物车
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        // 返回操作成功的结果
        return Result.success();
    }

    /**
     * 减少购物车中商品的数量
     * @param shoppingCartDTO 购物车数据传输对象，包含要减少的商品信息
     * @return 返回操作结果
     */
    @PostMapping("/sub")
    @ApiOperation(value = "减少购物车数量")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        // 记录减少购物车商品数量的日志信息
        log.info("减少购物车数量：{}", shoppingCartDTO);
        // 调用服务层方法减少购物车中商品的数量
        shoppingCartService.subShoppingCart(shoppingCartDTO);
        // 返回操作成功的结果
        return Result.success();
    }

    /**
     * 查看购物车列表
     * @return 返回购物车列表数据
     */
    @GetMapping("/list")
    @ApiOperation(value = "查看购物车")
    public Result<List<ShoppingCart>> list() {
        // 记录查看购物车的日志信息
        log.info("查看购物车");
        // 调用服务层方法获取购物车列表
        List<ShoppingCart> list = shoppingCartService.showShoppingCart();
        // 返回购物车列表数据
        return Result.success(list);
    }
}
