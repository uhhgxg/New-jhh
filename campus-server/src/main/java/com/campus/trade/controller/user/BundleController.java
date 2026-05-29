package com.campus.trade.controller.user;

import com.campus.trade.constant.StatusConstant;
import com.campus.trade.entity.Bundle;
import com.campus.trade.result.Result;
import com.campus.trade.service.BundleService;
import com.campus.trade.vo.BundleItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 捆绑包控制器
 * 处理C端用户对捆绑包的浏览相关请求
 */
@RestController("userBundleController")  // 使用@RestController注解标记为RESTful控制器，指定bean名称为"userBundleController"
@RequestMapping("/user/bundle")  // 设置请求的基础路径为"/user/bundle"
@Api(tags = "C端-捆绑包浏览接口")  // Swagger API文档注解，标记为C端捆绑包浏览接口
public class BundleController {
    @Autowired  // 自动注入BundleService实例
    private BundleService bundleService;



    /**
     * 根据分类id查询捆绑包列表
     * @param categoryId 分类ID
     * @return 返回捆绑包列表
     */
    @GetMapping("/list")  // 设置GET请求路径为"/list"
    @ApiOperation("根据分类id查询捆绑包")  // Swagger API文档注解，说明接口功能
    @Cacheable(value = "bundleCache", key = "#categoryId")  // 使用缓存，缓存名为bundleCache，key为categoryId
    public Result<List<Bundle>> list(Long categoryId) {
        // 创建捆绑包对象并设置分类ID和状态
        Bundle bundle = new Bundle();
        bundle.setCategoryId(categoryId);
        bundle.setStatus(StatusConstant.ENABLE);

        // 调用服务层查询符合条件的捆绑包列表
        List<Bundle> list = bundleService.list(bundle);
        // 返回查询结果
        return Result.success(list);
    }



    /**
     * 根据捆绑包id查询包含的商品列表
     * @param id 捆绑包ID
     * @return 返回捆绑包包含的商品列表
     */
    @GetMapping("/items/{id}")  // 设置GET请求路径为"/items/{id}"
    @ApiOperation("根据捆绑包id查询包含的商品列表")  // Swagger API文档注解，说明接口功能
    public Result<List<BundleItemVO>> itemsList(@PathVariable("id") Long id) {
        // 调用服务层查询指定捆绑包ID包含的商品列表
        List<BundleItemVO> list = bundleService.getItemByBundleId(id);
        // 返回查询结果
        return Result.success(list);
    }
}
