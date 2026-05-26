package com.sky.service;

import com.sky.entity.AddressBook;

/**
 * 百度地图服务接口
 */
public interface BaiduMapService {

    /**
     * 校验用户地址是否在配送范围内
     *
     * @param addressBook 用户地址
     * @return true: 在配送范围内; false: 超出配送范围
     */
    boolean isWithinDeliveryRange(AddressBook addressBook);
}
