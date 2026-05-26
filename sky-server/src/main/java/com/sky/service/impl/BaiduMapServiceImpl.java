package com.sky.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sky.entity.AddressBook;
import com.sky.exception.OrderBusinessException;
import com.sky.properties.ShopProperties;
import com.sky.service.BaiduMapService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 百度地图服务实现类
 * 用于地址编码和距离计算，校验用户地址是否在配送范围内
 */
@Service
@Slf4j
public class BaiduMapServiceImpl implements BaiduMapService {

    @Autowired
    private ShopProperties shopProperties;

    /**
     * 地理编码API地址
     */
    private static final String GEOCODING_URL = "https://api.map.baidu.com/geocoding/v3/";

    /**
     * 路线规划API地址（驾车）
     */
    private static final String DIRECTION_URL = "https://api.map.baidu.com/directionlite/v1/driving";

    /**
     * 校验用户地址是否在配送范围内
     *
     * @param addressBook 用户地址
     * @return true:在配送范围内 false:超出配送范围
     */
    public boolean isWithinDeliveryRange(AddressBook addressBook) {
        String shopAddress = shopProperties.getAddress();
        String baiduAk = shopProperties.getBaiduAk();

        if (baiduAk == null || baiduAk.isEmpty()) {
            log.warn("百度地图AK未配置，跳过配送范围校验");
            return true;
        }

        // 1. 获取商家门店坐标
        Map<String, String> shopLocation = getLocation(shopAddress, baiduAk);
        if (shopLocation == null) {
            log.error("获取商家门店坐标失败，地址：{}", shopAddress);
            throw new OrderBusinessException("获取商家门店坐标失败");
        }

        // 2. 获取用户收货地址坐标
        String userAddress = buildFullAddress(addressBook);
        Map<String, String> userLocation = getLocation(userAddress, baiduAk);
        if (userLocation == null) {
            log.error("获取用户地址坐标失败，地址：{}", userAddress);
            throw new OrderBusinessException("获取用户地址坐标失败");
        }

        // 3. 计算驾车距离
        int distance = getDrivingDistance(
                shopLocation.get("lat"), shopLocation.get("lng"),
                userLocation.get("lat"), userLocation.get("lng"),
                baiduAk
        );

        log.info("商家坐标：({}, {})", shopLocation.get("lat"), shopLocation.get("lng"));
        log.info("用户坐标：({}, {})", userLocation.get("lat"), userLocation.get("lng"));
        log.info("配送距离：{}米", distance);

        // 4. 判断是否在配送范围内（5公里 = 5000米）
        int deliveryRange = shopProperties.getDeliveryRange();
        if (distance > deliveryRange * 1000) {
            log.warn("用户地址超出配送范围，距离：{}米，配送范围：{}公里", distance, deliveryRange);
            return false;
        }

        return true;
    }

    /**
     * 根据地址获取经纬度坐标
     *
     * @param address 地址
     * @param ak      百度地图AK
     * @return 包含lat和lng的Map，失败返回null
     */
    private Map<String, String> getLocation(String address, String ak) {
        Map<String, String> params = new HashMap<>();
        params.put("address", address);
        params.put("output", "json");
        params.put("ak", ak);

        try {
            String result = HttpClientUtil.doGet(GEOCODING_URL, params);
            JSONObject jsonObject = JSONObject.parseObject(result);

            if (jsonObject != null && jsonObject.getIntValue("status") == 0) {
                JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
                Map<String, String> locationMap = new HashMap<>();
                locationMap.put("lat", location.getString("lat"));
                locationMap.put("lng", location.getString("lng"));
                return locationMap;
            } else {
                log.error("地理编码失败，响应：{}", result);
            }
        } catch (Exception e) {
            log.error("调用百度地图地理编码API异常", e);
        }

        return null;
    }

    /**
     * 获取驾车距离（单位：米）
     *
     * @param originLat     起点纬度
     * @param originLng     起点经度
     * @param destLat       终点纬度
     * @param destLng       终点经度
     * @param ak            百度地图AK
     * @return 距离（米），失败返回Integer.MAX_VALUE
     */
    private int getDrivingDistance(String originLat, String originLng, String destLat, String destLng, String ak) {
        Map<String, String> params = new HashMap<>();
        params.put("origin", originLat + "," + originLng);
        params.put("destination", destLat + "," + destLng);
        params.put("ak", ak);

        try {
            String result = HttpClientUtil.doGet(DIRECTION_URL, params);
            JSONObject jsonObject = JSONObject.parseObject(result);

            if (jsonObject != null && jsonObject.getIntValue("status") == 0) {
                JSONArray routes = jsonObject.getJSONObject("result").getJSONArray("routes");
                if (routes != null && routes.size() > 0) {
                    JSONObject route = routes.getJSONObject(0);
                    return route.getIntValue("distance");
                }
            } else {
                log.error("路线规划失败，响应：{}", result);
            }
        } catch (Exception e) {
            log.error("调用百度地图路线规划API异常", e);
        }

        return Integer.MAX_VALUE;
    }

    /**
     * 构建完整的用户地址
     */
    private String buildFullAddress(AddressBook addressBook) {
        StringBuilder sb = new StringBuilder();
        if (addressBook.getProvinceName() != null) {
            sb.append(addressBook.getProvinceName());
        }
        if (addressBook.getCityName() != null) {
            sb.append(addressBook.getCityName());
        }
        if (addressBook.getDistrictName() != null) {
            sb.append(addressBook.getDistrictName());
        }
        if (addressBook.getDetail() != null) {
            sb.append(addressBook.getDetail());
        }
        return sb.toString();
    }
}
