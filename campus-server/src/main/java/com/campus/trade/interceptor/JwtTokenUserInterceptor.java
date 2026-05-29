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
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 用户端JWT令牌校验拦截方法
     * <p>
     * 该方法在请求到达Controller之前执行，用于验证用户端的JWT令牌是否有效。
     * 如果令牌有效，则解析出用户ID并存储到BaseContext中供后续使用；
     * 如果令牌无效或缺失，则返回401状态码拒绝访问。
     * </p>
     *
     * @param request  HTTP请求对象，用于从请求头中获取JWT令牌
     * @param response HTTP响应对象，用于在校验失败时设置401状态码
     * @param handler  请求处理器对象，用于判断是否为Controller方法
     * @return true 表示校验通过，允许请求继续；false 表示校验失败，拒绝请求
     * @throws Exception 当JWT令牌解析过程中出现异常时抛出
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

    // 检查handler是否为HandlerMethod实例，如果不是则直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }


    // 从请求头中获取JWT令牌
        String token = request.getHeader(jwtProperties.getUserTokenName());
        
    // 记录Token校验开始的相关日志信息
        log.info("========== 用户端Token校验开始 ==========");
        log.info("请求路径：{} {}", request.getMethod(), request.getRequestURI());
        log.info("Token头名称：{}", jwtProperties.getUserTokenName());
        log.info("获取到的Token：{}", token == null ? "null" : (token.length() > 20 ? token.substring(0, 20) + "..." : token));


    // 尝试解析Token并处理用户信息
        try {
        // 解析JWT令牌获取Claims信息
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
        // 从Claims中提取用户ID
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
        // 将用户ID存储到BaseContext中
            BaseContext.setCurrentId(userId);
            
        // 记录Token校验成功的日志信息
            log.info("Token解析成功");
            log.info("当前用户ID：{}", userId);
            log.info("Token有效期：{}", claims.getExpiration());
            log.info("========== 用户端Token校验通过 ==========");
            

        // 校验通过，返回true允许请求继续
            return true;
        } catch (Exception ex) {
        // 记录Token校验失败的日志信息
            log.error("========== 用户端Token校验失败 ==========");
            log.error("失败原因：{}", ex.getMessage());
            log.error("异常类型：{}", ex.getClass().getSimpleName());
            
        // 根据Token是否存在进行问题诊断
            if (token == null || token.isEmpty()) {
                log.error("问题诊断：请求头中未携带Token，请检查前端是否在header中添加了'authentication'字段");
            } else {
                log.error("问题诊断：Token格式错误或已过期，请检查Token是否完整");
            }
            

        // 设置响应状态码为401（未授权）
            response.setStatus(401);
        // 校验失败，返回false拒绝请求
            return false;
        }
    }
}
