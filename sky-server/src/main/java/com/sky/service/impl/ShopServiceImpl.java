package com.sky.service.impl;



import com.sky.constant.StatusConstant;
import com.sky.entity.Shop;
import com.sky.mapper.ShopMapper;
import com.sky.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ShopServiceImpl implements ShopService {

    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ShopMapper shopMapper;

    @Override
    public void setStatus(Integer status) {
        log.info("设置店铺状态为: {}", status == 1 ? "营业" : "打烊");

        Shop shop = Shop.builder()
                .id(1L)
                .status(status)
                .build();
        shopMapper.update(shop);

        redisTemplate.opsForValue().set(KEY, status, 24, TimeUnit.HOURS);
        
        log.info("店铺状态已更新到Redis，key={}, value={}", KEY, status);
    }

    @Override
    public Integer getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        
        if (status != null) {
            log.info("从Redis获取店铺状态: {}", status == 1 ? "营业" : "打烊");
            return status;
        }

        log.info("Redis中无店铺状态，从数据库查询...");
        Shop shop = shopMapper.getById(1L);
        if (shop != null) {
            status = shop.getStatus();
            redisTemplate.opsForValue().set(KEY, status, 24, TimeUnit.HOURS);
            log.info("从数据库获取店铺状态并缓存到Redis: {}", status == 1 ? "营业" : "打烊");
            return status;
        }

        log.info("数据库中无店铺状态，返回默认营业状态");
        return StatusConstant.ENABLE;
    }
}
