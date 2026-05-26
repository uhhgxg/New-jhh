package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.shop")
@Data
public class ShopProperties {

    /**
     * 商家门店地址
     */
    private String address;

    /**
     * 百度地图AK
     */
    private String baiduAk;

    /**
     * 配送范围（公里），默认5公里
     */
    private int deliveryRange = 5;
}
