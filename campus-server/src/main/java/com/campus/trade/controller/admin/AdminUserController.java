package com.campus.trade.controller.admin;

import com.campus.trade.constant.JwtClaimsConstant;
import com.campus.trade.dto.AdminDTO;
import com.campus.trade.dto.AdminLoginDTO;
import com.campus.trade.dto.AdminPageQueryDTO;
import com.campus.trade.dto.PasswordEditDTO;
import com.campus.trade.entity.Admin;
import com.campus.trade.properties.JwtProperties;
import com.campus.trade.result.PageResult;
import com.campus.trade.result.Result;
import com.campus.trade.service.AdminService;
import com.campus.trade.utils.JwtUtil;
import com.campus.trade.vo.AdminLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员用户控制器
 * 提供管理员相关的API接口，包括登录、新增、查询、状态修改、信息编辑、密码修改和退出等功能
 */
@RestController
@RequestMapping("/admin/admin")
@Slf4j
@Api(tags = "管理员管理")
public class AdminUserController {

    /**
     * 注入管理员服务接口
     */
    @Autowired
    private AdminService adminService;
    /**
     * 注入JWT属性配置
     */
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 管理员登录接口
     * @param adminLoginDTO 管理员登录数据传输对象，包含用户名和密码等信息
     * @return 返回登录结果，包含管理员ID、用户名、姓名和JWT令牌等信息
     */
    @PostMapping("/login")
    @ApiOperation("管理员登录")
    public Result<AdminLoginVO> login(@RequestBody AdminLoginDTO adminLoginDTO) {
        log.info("管理员登录：{}", adminLoginDTO);

        Admin admin = adminService.login(adminLoginDTO);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ADMIN_ID, admin.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        AdminLoginVO adminLoginVO = AdminLoginVO.builder()
                .id(admin.getId())
                .userName(admin.getUsername())
                .name(admin.getName())
                .token(token)
                .build();

        return Result.success(adminLoginVO);
    }

    /**
     * 新增管理员接口
     * @param adminDTO 管理员数据传输对象，包含管理员基本信息
     * @return 返回操作结果
     */
    @PostMapping
    @ApiOperation("新增管理员")
    public Result save(@RequestBody AdminDTO adminDTO) {
        log.info("新增管理员：{}", adminDTO);
        adminService.save(adminDTO);
        return Result.success();
    }

    /**
     * 管理员分页查询接口
     * @param adminPageQueryDTO 管理员分页查询条件
     * @return 返回分页查询结果，包含当前页数据和总记录数等信息
     */
    @GetMapping("/page")
    @ApiOperation("管理员分页查询")
    public Result<PageResult> page(AdminPageQueryDTO adminPageQueryDTO) {
        log.info("分页查询管理员：{}", adminPageQueryDTO);
        PageResult pageResult = adminService.pageQuery(adminPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 修改管理员状态接口
     * @param status 要修改的状态值
     * @param id 管理员ID
     * @return 返回操作结果
     */
    @PostMapping("/status/{status}")
    @ApiOperation("修改管理员状态")
    public Result updateStatus(@PathVariable Integer status, @RequestParam Long id) {
        log.info("修改管理员状态，id：{}，status：{}", id, status);
        adminService.updateStatus(status, id);
        return Result.success();
    }

    /**
     * 根据ID查询管理员信息接口
     * @param id 管理员ID
     * @return 返回查询到的管理员信息
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询管理员信息")
    public Result<Admin> getById(@PathVariable Long id) {
        log.info("根据id查询管理员信息，id：{}", id);
        Admin admin = adminService.getById(id);
        return Result.success(admin);
    }

    /**
     * 编辑管理员信息接口
     * @param adminDTO 管理员数据传输对象，包含要修改的管理员信息
     * @return 返回操作结果
     */
    @PutMapping
    @ApiOperation("编辑管理员信息")
    public Result update(@RequestBody AdminDTO adminDTO) {
        log.info("编辑管理员信息：{}", adminDTO);
        adminService.update(adminDTO);
        return Result.success();
    }

    /**
     * 修改管理员密码接口
     * @param passwordEditDTO 密码修改数据传输对象，包含原密码和新密码等信息
     * @return 返回操作结果
     */
    @PutMapping("/editPassword")
    @ApiOperation("修改密码")
    public Result editPassword(@RequestBody PasswordEditDTO passwordEditDTO) {
        log.info("修改密码：{}", passwordEditDTO);
        adminService.editPassword(passwordEditDTO);
        return Result.success();
    }

    /**
     * 管理员退出登录接口
     * @return 返回操作结果
     */
    @PostMapping("/logout")
    @ApiOperation("管理员退出")
    public Result<String> logout() {
        log.info("管理员退出登录");
        return Result.success();
    }
}
