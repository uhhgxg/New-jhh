package com.campus.trade.interceptor;

import com.campus.trade.constant.JwtClaimsConstant;
import com.campus.trade.context.BaseContext;
import com.campus.trade.properties.JwtProperties;
import com.campus.trade.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器
 * 用于管理端请求的JWT令牌验证
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getAdminTokenName());
        
        log.info("========== 管理端Token校验开始 ==========");
        log.info("请求路径：{} {}", request.getMethod(), request.getRequestURI());
        log.info("Token头名称：{}", jwtProperties.getAdminTokenName());
        log.info("获取到的Token：{}", token == null ? "null" : (token.length() > 20 ? token.substring(0, 20) + "..." : token));

        //2、校验令牌
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long adminId = Long.valueOf(claims.get(JwtClaimsConstant.ADMIN_ID).toString());
            BaseContext.setCurrentId(adminId);

            log.info("Token解析成功");
            log.info("当前管理员ID：{}", adminId);
            log.info("Token有效期：{}", claims.getExpiration());
            log.info("========== 管理端Token校验通过 ==========");
            
            //3、通过，放行
            return true;
        } catch (Exception ex) {
            log.error("========== 管理端Token校验失败 ==========");
            log.error("失败原因：{}", ex.getMessage());
            log.error("异常类型：{}", ex.getClass().getSimpleName());
            
            if (token == null || token.isEmpty()) {
                log.error("问题诊断：请求头中未携带Token，请检查前端是否在header中添加了'token'字段");
            } else {
                log.error("问题诊断：Token格式错误或已过期，请检查Token是否完整");
            }
            
            //4、不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }
}
