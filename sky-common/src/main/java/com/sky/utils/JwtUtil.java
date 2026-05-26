package com.sky.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
/**
 * JWT工具类，用于生成和解析JWT令牌
 * 提供了创建JWT令牌和解析JWT令牌的方法
 */
public class JwtUtil {

    /**
     * 创建JWT令牌
     * @param secretKey 加密密钥，用于签名JWT
     * @param ttlMillis 令牌有效时间（毫秒）
     * @param claims 自定义声明，存储在JWT中的数据
     * @return 返回生成的JWT字符串
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 使用密钥创建HMAC-SHA算法的密钥对象
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        // 计算过期时间
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        // 使用Jwts构建器创建JWT令牌
        return Jwts.builder()
                .claims(claims)  // 设置自定义声明
                .expiration(exp) // 设置过期时间
                .signWith(key)  // 使用密钥进行签名
                .compact();     // 构建并返回JWT字符串
    }

    /**
     * 解析JWT令牌
     * @param secretKey 加密密钥，用于验证JWT签名
     * @param token JWT令牌字符串
     * @return 返回JWT中的声明（Claims）对象
     */
    public static Claims parseJWT(String secretKey, String token) {
        // 使用密钥创建HMAC-SHA算法的密钥对象
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        // 使用Jwts解析器解析JWT令牌并返回声明
        return Jwts.parser()
                .verifyWith(key)    // 设置验证密钥
                .build()            // 构建解析器
                .parseSignedClaims(token)  // 解析签名声明
                .getPayload();      // 获取负载（声明）
    }
}
