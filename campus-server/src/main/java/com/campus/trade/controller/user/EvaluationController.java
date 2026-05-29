package com.campus.trade.controller.user;

import com.campus.trade.dto.EvaluationDTO;
import com.campus.trade.result.Result;
import com.campus.trade.service.EvaluationService;
import com.campus.trade.vo.EvaluationVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评价管理控制器
 * 提供评价的创建、查询等功能接口
 */
@RestController
@RequestMapping("/user/evaluation")
@Slf4j
@Api(tags = "评价管理")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;  // 评价服务接口

    /**
     * 创建评价接口
     * @param evaluationDTO 评价数据传输对象
     * @return 返回操作结果
     */
    @PostMapping
    @ApiOperation("创建评价")
    public Result create(@RequestBody EvaluationDTO evaluationDTO) {
        log.info("创建评价：{}", evaluationDTO);  // 记录创建评价的日志
        evaluationService.createEvaluation(evaluationDTO);  // 调用服务层创建评价
        return Result.success();  // 返回成功结果
    }

    /**
     * 查看用户收到的评价列表接口
     * @param userId 用户ID
     * @return 返回评价列表
     */
    @GetMapping("/user/{userId}")
    @ApiOperation("查看用户收到的评价")
    public Result<List<EvaluationVO>> getByUser(@PathVariable Long userId) {
        log.info("查看用户评价，userId：{}", userId);  // 记录查看用户评价的日志
        List<EvaluationVO> list = evaluationService.getEvaluationsByUser(userId);  // 调用服务层获取用户评价列表
        return Result.success(list);  // 返回评价列表结果
    }
}
