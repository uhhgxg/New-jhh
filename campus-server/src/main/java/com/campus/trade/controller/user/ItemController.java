package com.campus.trade.controller.user;

import com.campus.trade.constant.StatusConstant;
import com.campus.trade.dto.ItemSearchDTO;
import com.campus.trade.entity.Item;
import com.campus.trade.result.PageResult;
import com.campus.trade.result.Result;
import com.campus.trade.service.ItemService;
import com.campus.trade.vo.ItemDetailVO;
import com.campus.trade.vo.ItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端商品浏览控制器
 * 提供商品列表查询、商品详情查看、商品搜索和卖家商品列表等功能
 */
@RestController("userItemController")  // 声明为REST控制器，指定bean名称为"userItemController"
@RequestMapping("/user/item")  // 设置基础请求路径为"/user/item"
@Slf4j  // Lombok日志注解，自动生成日志器
@Api(tags = "C端-商品浏览")  // Swagger API文档注解，标记为C端商品浏览模块
public class ItemController {
    // 注入商品服务
    @Autowired
    private ItemService itemService;
    // 注入Redis模板
    @Autowired
    private RedisTemplate redisTemplate;



    /**
     * 根据分类id查询商品列表
     * @param categoryId 分类ID
     * @return 返回商品列表结果
     */
    @GetMapping("/list")  // GET请求映射到"/list"
    @ApiOperation("根据分类id查询商品")
    public Result<List<ItemVO>> list(Long categoryId) {
        String key = "item_" + categoryId;
        List<ItemVO> list = (List<ItemVO>) redisTemplate.opsForValue().get(key);
        if (list != null && list.size() > 0) {
            return Result.success(list);
        }

        Item item = new Item();
        item.setCategoryId(categoryId);
        item.setSaleStatus(StatusConstant.ON_SALE);

        list = itemService.listWithCondition(item);
        redisTemplate.opsForValue().set(key, list);

        return Result.success(list);
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("根据id查询商品详情")
    public Result<ItemDetailVO> getDetail(@PathVariable Long id) {
        log.info("查询商品详情，id：{}", id);
        itemService.incrementViewCount(id);
        ItemDetailVO itemDetailVO = itemService.getDetailById(id);
        return Result.success(itemDetailVO);
    }

    @GetMapping("/search")
    @ApiOperation("搜索商品")
    public Result<PageResult> search(ItemSearchDTO itemSearchDTO) {
        log.info("搜索商品：{}", itemSearchDTO);
        PageResult pageResult = itemService.search(itemSearchDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/seller/{sellerId}")
    @ApiOperation("查看卖家主页（上架商品列表）")
    public Result<List<ItemVO>> sellerItems(@PathVariable Long sellerId) {
        log.info("查看卖家主页，sellerId：{}", sellerId);
        List<ItemVO> list = itemService.listBySellerId(sellerId);
        return Result.success(list);
    }
}
