package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * UserServiceImpl类是UserService接口的实现类
 * 提供用户相关的服务实现
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    WeChatProperties weChatProperties;
    @Autowired
    UserMapper userMapper;
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    /**
     * 微信登录方法实现
     *
     * @param userLoginDTO 用户登录数据传输对象，包含微信登录所需的信息
     * @return 返回User对象，包含用户的基本信息
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        log.info("开始微信登录，code：{}", userLoginDTO.getCode());
        String openid = getOpenid(userLoginDTO);
        if (openid == null) {
            log.error("微信登录失败，获取openid为空");
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        log.info("微信登录成功，openid：{}", openid);

        User user = userMapper.wxLogin(openid);
        if (user == null) {
            log.info("新用户注册，openid：{}", openid);
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
            log.info("新用户注册成功，userId：{}", user.getId());
        } else {
            log.info("老用户登录，userId：{}", user.getId());
        }
        return user;
    }

    /**
     * 调用微信接口获取openid
     * @param userLoginDTO 用户登录数据传输对象
     * @return 微信用户的openid
     */
    private String getOpenid(UserLoginDTO userLoginDTO) {
        Map<String,String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", userLoginDTO.getCode());
        map.put("grant_type", "authorization_code");
        
        String json = HttpClientUtil.doGet(WX_LOGIN, map);
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString("openid");
    }
}
