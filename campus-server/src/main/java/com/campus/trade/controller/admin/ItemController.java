package com.campus.trade.controller.admin;

import com.campus.trade.dto.ItemDTO;
import com.campus.trade.dto.ItemPageQueryDTO;
import com.campus.trade.result.PageResult;
import com.campus.trade.result.Result;
import com.campus.trade.service.ItemService;
import com.campus.trade.vo.ItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 商品管理控制器
 * 提供商品的增删改查、状态管理等接口
 */
@RestController
@RequestMapping("/admin/item")
@Slf4j
@Api(tags = "商品管理")
public class ItemController {

    @Autowired
    private ItemService itemService;    // 商品服务接口
    @Autowired
    private RedisTemplate redisTemplate;  // Redis模板，用于缓存操作

    /**
     * 新增商品
     * @param itemDTO 商品数据传输对象
     * @return 操作结果
     */
    @PostMapping
    @ApiOperation("新增商品")
    public Result save(@RequestBody ItemDTO itemDTO) {
        log.info("新增商品：{}", itemDTO);
        itemService.saveItem(itemDTO);
        cleanRedisCache("item_" + itemDTO.getCategoryId());  // 清除相关缓存
        return Result.success();
    }

    /**
     * 分页查询商品
     * @param itemPageQueryDTO 商品分页查询条件
     * @return 分页查询结果
     */
    @GetMapping("/page")
    @ApiOperation("分页查询商品")
    public Result<PageResult> page(ItemPageQueryDTO itemPageQueryDTO) {
        log.info("分页查询商品：{}", itemPageQueryDTO);
        PageResult pageResult = itemService.page(itemPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除商品
     * @param ids 商品ID列表
     * @return 操作结果
     */
    @DeleteMapping
    @ApiOperation("批量删除商品")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除商品：{}", ids);
        itemService.deleteByIds(ids);
        cleanRedisCache("item_*");  // 清除所有商品缓存
        return Result.success();
    }

    /**
     * 修改商品
     * @param itemDTO 商品数据传输对象
     * @return 操作结果
     */
    @PutMapping
    @ApiOperation("修改商品")
    public Result update(@RequestBody ItemDTO itemDTO) {
        log.info("修改商品：{}", itemDTO);
        itemService.updateItem(itemDTO);
        cleanRedisCache("item_" + itemDTO.getCategoryId());  // 清除相关缓存
        return Result.success();
    }

    /**
     * 根据id查询商品
     * @param id 商品ID
     * @return 商品信息
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询商品")
    public Result<ItemVO> getById(@PathVariable Long id) {
        log.info("根据id查询商品：{}", id);
        ItemVO itemVO = itemService.getById(id);
        return Result.success(itemVO);
    }

    /**
     * 根据分类id查询商品
     * @param categoryId 分类ID
     * @return 商品列表
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询商品")
    public Result<List<com.campus.trade.entity.Item>> list(Long categoryId) {
        log.info("根据分类id查询商品，categoryId：{}", categoryId);
        List<com.campus.trade.entity.Item> list = itemService.list(categoryId);
        log.info("查询到商品数量：{}", list == null ? 0 : list.size());
        return Result.success(list);
    }

    /**
     * 修改商品上架/下架状态
     * @param saleStatus 销售状态（上架/下架）
     * @param id 商品ID
     * @return 操作结果
     */
    @PostMapping("/status/{saleStatus}")
    @ApiOperation("修改商品上架/下架状态")
    public Result updateSaleStatus(@PathVariable Integer saleStatus, Long id) {
        log.info("修改商品状态，saleStatus：{}，id：{}", saleStatus, id);
        itemService.updateSaleStatus(saleStatus, id);
        cleanRedisCache("item_*");  // 清除所有商品缓存
        return Result.success();
    }

    /**
     * 清理Redis缓存
     * @param key 缓存键
     */
    private void cleanRedisCache(String key) {
        log.info("清理Redis缓存，key：{}", key);
        Set<String> keys = redisTemplate.keys(key);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("成功删除缓存键数量：{}", keys.size());
        } else {
            log.info("未找到匹配的缓存键");
        }
    }
}
