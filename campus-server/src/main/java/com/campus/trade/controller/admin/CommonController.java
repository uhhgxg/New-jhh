package com.campus.trade.controller.admin;

import com.campus.trade.result.Result;
import com.campus.trade.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用控制器，提供文件上传等通用功能接口
 * 使用@RestController注解标记为RESTful控制器
 * @RequestMapping("/admin/common")指定基础请求路径
 * @Slf4j提供日志支持
 * @Api用于Swagger API文档生成
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "通用接口")
public class CommonController {
    
    /**
     * 自动注入阿里云OSS工具类
     * 用于处理文件上传到阿里云OSS存储
     */
    @Autowired
    private AliOssUtil aliOssUtil;
    
    /**
     * 处理文件上传请求
     * @param file 上传的文件，通过MultipartFile接收
     * @return 返回Result对象，包含上传后的文件访问路径
     * @PostMapping("/upload")指定POST请求方式和具体路径
     * @ApiOperation用于Swagger API文档生成，描述接口功能
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {
        // 记录开始上传文件的日志
        log.info("开始上传文件：{}", file.getOriginalFilename());

        try {
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            // 获取文件扩展名
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 生成唯一文件名
            String objectName = UUID.randomUUID().toString() + extension;
            // 调用阿里云OSS工具类上传文件
            String path = aliOssUtil.upload(file.getBytes(), objectName);
            
            // 返回成功结果，包含文件访问路径
            return Result.success(path);
        } catch (IOException e) {
            // 记录文件上传失败的错误日志
            log.error("文件上传失败：{}", e.getMessage());
            // 返回失败结果
            return Result.error("文件上传失败");
        }
    }

}
