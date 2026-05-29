package com.campus.trade.controller.user;

import com.campus.trade.constant.StatusConstant;
import com.campus.trade.context.BaseContext;
import com.campus.trade.dto.ItemDTO;
import com.campus.trade.entity.Item;
import com.campus.trade.result.Result;
import com.campus.trade.service.ItemService;
import com.campus.trade.vo.ItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品发布控制器
 * 提供商品发布、修改、状态查询等功能
 */
@RestController
@RequestMapping("/user/publish")
@Slf4j
@Api(tags = "商品发布")
public class ItemPublishController {

    @Autowired
    private ItemService itemService;  // 注入商品服务

    /**
     * 发布商品
     * @param itemDTO 商品数据传输对象
     * @return 返回操作结果
     */
    @PostMapping
    @ApiOperation("发布商品")
    public Result publish(@RequestBody ItemDTO itemDTO) {
        log.info("发布商品：{}", itemDTO);  // 记录发布商品的日志
        itemDTO.setSellerId(BaseContext.getCurrentId());  // 设置卖家ID
        itemDTO.setSaleStatus(StatusConstant.ON_SALE);  // 设置商品状态为上架
        itemService.saveItem(itemDTO);  // 保存商品信息
        return Result.success();  // 返回成功结果
    }

    /**
     * 修改已发布商品
     * @param itemDTO 商品数据传输对象
     * @return 返回操作结果
     */
    @PutMapping
    @ApiOperation("修改已发布商品")
    public Result update(@RequestBody ItemDTO itemDTO) {
        log.info("修改已发布商品：{}", itemDTO);  // 记录修改商品的日志
        itemService.updateItem(itemDTO);  // 更新商品信息
        return Result.success();  // 返回成功结果
    }

    /**
     * 修改商品上架/下架状态
     * @param saleStatus 销售状态（上架/下架）
     * @param 商品ID
     * @return 返回操作结果
     */
    @PostMapping("/status/{saleStatus}/{id}")
    @ApiOperation("修改商品上架/下架状态")
    public Result updateStatus(@PathVariable Integer saleStatus, @PathVariable Long id) {
        log.info("修改商品状态，saleStatus：{}，id：{}", saleStatus, id);  // 记录修改商品状态的日志
        itemService.updateSaleStatus(saleStatus, id);  // 更新商品销售状态
        return Result.success();  // 返回成功结果
    }

    /**
     * 查看我的已发布商品
     * @return 返回商品列表
     */
    @GetMapping("/myItems")
    @ApiOperation("查看我的已发布商品")
    public Result<List<ItemVO>> myItems() {
        Long sellerId = BaseContext.getCurrentId();  // 获取当前用户ID
        log.info("查看我的已发布商品，sellerId：{}", sellerId);  // 记录查看商品的日志
        Item item = new Item();
        item.setSellerId(sellerId);  // 设置查询条件为当前用户
        List<ItemVO> list = itemService.listWithCondition(item);  // 查询符合条件的商品
        return Result.success(list);  // 返回商品列表
    }
}
