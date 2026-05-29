package com.campus.trade.controller.user;

import com.campus.trade.constant.JwtClaimsConstant;
import com.campus.trade.dto.UserLoginDTO;
import com.campus.trade.entity.User;
import com.campus.trade.properties.JwtProperties;
import com.campus.trade.result.Result;
import com.campus.trade.service.UserService;
import com.campus.trade.utils.JwtUtil;
import com.campus.trade.vo.UserLoginVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
/**
 * C端用户控制器
 * 处理用户登录等相关请求
 */
@Slf4j
@RestController
@RequestMapping("/user/user")
@Api(tags = "c端用户相关接口")
public class UserController {
    /**
     * 用户服务接口
     */
    @Autowired
    private UserService userService;
    /**
     * JWT属性配置
     */
    @Autowired
    private JwtProperties jwtProperties;
    /**
     * 用户微信登录接口
     * @param userLoginDTO 用户登录DTO，包含微信登录code
     * @return 返回登录结果，包含用户信息和token
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        // 记录用户登录日志
        log.info("用户端微信登录，入参：{}", userLoginDTO.getCode());
        // 调用服务层处理微信登录
        User user = userService.wxLogin(userLoginDTO);
        
        // 构建JWT claims
        Map<String, Object> claims = new HashMap<>();
        // 修改点：使用 JwtClaimsConstant.USER_ID 常量，确保与拦截器一致
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        
        // 生成JWT token
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        
        // 构建登录VO对象
        UserLoginVO userLoginVO = new UserLoginVO();
        // 复制用户属性到VO
        BeanUtils.copyProperties(user, userLoginVO);
        // 设置token
        userLoginVO.setToken(token);
        
        // 返回成功结果
        return Result.success(userLoginVO);
    }
}
