package com.campus.trade.controller.user;

import com.campus.trade.context.BaseContext;
import com.campus.trade.dto.FavoriteDTO;
import com.campus.trade.result.PageResult;
import com.campus.trade.result.Result;
import com.campus.trade.service.FavoriteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏管理控制器
 * 提供收藏相关的RESTful API接口
 */
@RestController
@RequestMapping("/user/favorite")
@Slf4j
@Api(tags = "收藏管理")
public class FavoriteController {

    /**
     * 注入收藏服务接口实现类
     */
    @Autowired
    private FavoriteService favoriteService;

    /**
     * 添加收藏接口
     * @param favoriteDTO 收藏数据传输对象，包含收藏相关信息
     * @return 返回操作结果
     */
    @PostMapping
    @ApiOperation("添加收藏")
    public Result add(@RequestBody FavoriteDTO favoriteDTO) {
        log.info("添加收藏：{}", favoriteDTO);
        favoriteService.addFavorite(favoriteDTO);
        return Result.success();
    }

    /**
     * 取消收藏接口
     * @param itemId 商品ID，用于标识要取消收藏的商品
     * @return 返回操作结果
     */
    @DeleteMapping("/{itemId}")
    @ApiOperation("取消收藏")
    public Result remove(@PathVariable Long itemId) {
        log.info("取消收藏，itemId：{}", itemId);
        favoriteService.removeFavorite(itemId);
        return Result.success();
    }

    /**
     * 分页查询收藏接口
     * @param page 当前页码，默认为1
     * @param pageSize 每页显示数量，默认为10
     * @return 返回分页查询结果，包含收藏列表和分页信息
     */
    @GetMapping("/page")
    @ApiOperation("分页查询收藏")
    public Result<PageResult> page(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int pageSize) {
        log.info("分页查询收藏，page：{}，pageSize：{}", page, pageSize);
        PageResult pageResult = favoriteService.pageQuery(page, pageSize);
        return Result.success(pageResult);
    }

    @GetMapping("/check/{itemId}")
    @ApiOperation("检查是否已收藏")
    public Result<Boolean> check(@PathVariable Long itemId) {
        log.info("检查是否已收藏，itemId：{}", itemId);
        boolean favorited = favoriteService.isFavorited(itemId);
        return Result.success(favorited);
    }
}
