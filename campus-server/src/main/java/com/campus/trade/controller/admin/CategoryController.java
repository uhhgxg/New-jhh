package com.campus.trade.controller.admin;

import com.campus.trade.dto.CategoryDTO;
import com.campus.trade.dto.CategoryPageQueryDTO;
import com.campus.trade.entity.Category;
import com.campus.trade.result.PageResult;
import com.campus.trade.result.Result;
import com.campus.trade.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 分类管理
 * 提供分类相关的RESTful API接口，包括新增、查询、删除、修改、启用/禁用以及按类型查询分类等功能
 */
@RestController
@RequestMapping("/admin/category")
@Api(tags = "分类相关接口")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param categoryDTO 包含分类信息的DTO对象
     * @return 返回操作结果，成功为success
     */
    @PostMapping
    @ApiOperation("新增分类")
    public Result<String> save(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类：{}", categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO 包含分页查询条件的DTO对象
     * @return 返回分页查询结果，包含分类列表和分页信息
     */
    @GetMapping("/page")
    @ApiOperation("分类分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分页查询：{}", categoryPageQueryDTO);
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 删除分类
     * @param id 分类ID
     * @return 返回操作结果，成功为success
     */
    @DeleteMapping
    @ApiOperation("删除分类")
    public Result<String> deleteById(Long id){
        log.info("删除分类：{}", id);
        categoryService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改分类
     * @param categoryDTO 包含分类更新信息的DTO对象
     * @return 返回操作结果，成功为success
     */
    @PutMapping
    @ApiOperation("修改分类")
    public Result<String> update(@RequestBody CategoryDTO categoryDTO){
        log.info("修改分类：{}", categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }

    /**
     * 启用、禁用分类
     * @param status 分类状态（1启用，0禁用）
     * @param id 分类ID
     * @return 返回操作结果，成功为success
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用分类")
    public Result<String> startOrStop(@PathVariable("status") Integer status, Long id){
        log.info("启用/禁用分类，id：{}，status：{}", id, status);
        categoryService.startOrStop(status,id);
        return Result.success();
    }

    /**
     * 根据类型查询分类
     * @param type 分类类型
     * @return 返回指定类型的分类列表
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result<List<Category>> list(Integer type){
        log.info("根据类型查询分类，type：{}", type);
        List<Category> list = categoryService.list(type);
        log.info("查询到分类数量：{}", list == null ? 0 : list.size());
        return Result.success(list);
    }
}
