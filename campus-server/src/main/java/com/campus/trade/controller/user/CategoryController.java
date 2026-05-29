package com.campus.trade.controller.user;

import com.campus.trade.entity.Category;
import com.campus.trade.result.Result;
import com.campus.trade.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * C端分类控制器
 * 提供分类相关的REST API接口
 */
@RestController("userCategoryController")  // 声明为REST控制器，指定bean名称为userCategoryController
@RequestMapping("/user/category")  // 设置基础请求路径为/user/category
@Slf4j  // Lombok日志注解，自动生成log对象
@Api(tags = "C端-分类接口")  // Swagger API文档注解，定义接口分组为C端分类接口
public class CategoryController {

    @Autowired  // 自动注入CategoryService实例
    private CategoryService categoryService;

    /**
     * 查询分类列表接口
     * @param type 分类类型参数
     * @return 返回Result对象，包含分类列表数据
     */
    @GetMapping("/list")
    @ApiOperation("查询分类")
    public Result<List<Category>> list(Integer type) {
        log.info("查询分类，类型：{}", type);
        List<Category> list = categoryService.list(type);
        log.info("查询结果数量：{}", list == null ? 0 : list.size());
        return Result.success(list);
    }
}
