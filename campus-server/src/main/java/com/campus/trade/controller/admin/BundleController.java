package com.campus.trade.controller.admin;

import com.campus.trade.dto.BundleDTO;
import com.campus.trade.dto.BundlePageQueryDTO;
import com.campus.trade.result.PageResult;
import com.campus.trade.result.Result;
import com.campus.trade.service.BundleService;
import com.campus.trade.vo.BundleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 捆绑包管理控制器
 * 提供捆绑包的增删改查、状态管理等API接口
 */
@RestController
@RequestMapping("/admin/bundle")
@Slf4j
@Api(tags = "捆绑包管理")
public class BundleController {

    @Autowired
    private BundleService bundleService; // 捆绑包服务接口，用于处理业务逻辑

    /**
     * 新增捆绑包
     * @param bundleDTO 捆绑包数据传输对象，包含捆绑包的基本信息
     * @return 返回操作结果
     */
    @PostMapping
    @ApiOperation("新增捆绑包")
    public Result save(@RequestBody BundleDTO bundleDTO) {
        log.info("新增捆绑包：{}", bundleDTO); // 记录日志，输出新增的捆绑包信息
        bundleService.saveWithItem(bundleDTO); // 调用服务层方法保存捆绑包及其关联项目
        return Result.success(); // 返回成功结果
    }

    /**
     * 分页查询捆绑包
     * @param bundlePageQueryDTO 捆绑包分页查询条件
     * @return 返回分页查询结果
     */
    @GetMapping("/page")
    @ApiOperation("分页查询捆绑包")
    public Result<PageResult> page(BundlePageQueryDTO bundlePageQueryDTO) {
        log.info("分页查询捆绑包：{}", bundlePageQueryDTO); // 记录日志，输出查询条件
        PageResult pageResult = bundleService.pageQuery(bundlePageQueryDTO); // 调用服务层方法进行分页查询
        return Result.success(pageResult); // 返回查询结果
    }

    /**
     * 批量删除捆绑包
     * @param ids 要删除的捆绑包ID列表
     * @return 返回操作结果
     */
    @DeleteMapping
    @ApiOperation("批量删除捆绑包")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除捆绑包：{}", ids); // 记录日志，输出要删除的ID列表
        bundleService.deleteByIds(ids); // 调用服务层方法批量删除
        return Result.success(); // 返回成功结果
    }

    /**
     * 根据ID查询捆绑包
     * @param id 捆绑包ID
     * @return 返回捆绑包详细信息
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询捆绑包")
    public Result<BundleVO> getById(@PathVariable Long id) {
        log.info("根据id查询捆绑包：{}", id); // 记录日志，输出查询的ID
        BundleVO bundleVO = bundleService.getByIdWithItem(id); // 调用服务层方法获取捆绑包及其关联项目
        return Result.success(bundleVO); // 返回查询结果
    }

    /**
     * 修改捆绑包
     * @param bundleDTO 捆绑包数据传输对象，包含更新后的捆绑包信息
     * @return 返回操作结果
     */
    @PutMapping
    @ApiOperation("修改捆绑包")
    public Result update(@RequestBody BundleDTO bundleDTO) {
        log.info("修改捆绑包：{}", bundleDTO); // 记录日志，输出修改的捆绑包信息
        bundleService.updateWithItem(bundleDTO); // 调用服务层方法更新捆绑包及其关联项目
        return Result.success(); // 返回成功结果
    }

    /**
     * 启售或停售捆绑包
     * @param status 状态：1-启售，0-停售
     * @param id 捆绑包ID
     * @return 返回操作结果
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启售/停售捆绑包")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("启售/停售捆绑包：status={}，id={}", status, id); // 记录日志，输出状态和ID
        bundleService.startOrStop(status, id); // 调用服务层方法更新状态
        return Result.success(); // 返回成功结果
    }
}
