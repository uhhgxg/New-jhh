package com.campus.trade.controller.notify;

import com.campus.trade.service.TradeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付通知控制器
 * 处理支付相关的回调通知，特别是微信支付的成功回调
 */
@Slf4j
@RestController
@RequestMapping("/notify")
public class PayNotifyController {

    /**
     * 注入交易订单服务
     * 用于处理支付成功后的业务逻辑
     */
    @Autowired
    private TradeOrderService tradeOrderService;

    /**
     * 微信支付回调接口
     * 该接口用于接收微信支付成功后的异步通知
     * 处理流程：

     * 1. 读取回调通知内容
     * 2. 解析回调数据获取订单号
     * 3. 验证回调数据（实际项目中需要验证签名）
     * 4. 调用服务层处理支付成功逻辑
     * 5. 返回处理结果给微信支付
     *
     * @param request HTTP请求对象，包含微信支付回调数据
     * @param response HTTP响应对象，用于返回处理结果
     * @return 返回给微信支付的结果，JSON格式
     */
    @RequestMapping("/paySuccess")
    public String paySuccess(HttpServletRequest request, HttpServletResponse response) {
        log.info("收到微信支付回调通知");

        // 读取回调通知内容
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (Exception e) {
            log.error("读取回调通知失败", e);
            return responseFail("读取回调通知失败");
        }

        String notifyData = stringBuilder.toString();
        log.info("回调通知数据：{}", notifyData);

        // TODO: 实际项目中需要验证微信签名，确保回调数据的真实性
        // 这里简化处理，直接解析订单号并处理支付成功逻辑

        try {
            // 模拟解析回调数据获取订单号
            // 实际项目中应该解析JSON数据并验证签名
            Map<String, String> notifyMap = parseNotifyData(notifyData);

            // 从回调数据中获取商户订单号
            String outTradeNo = notifyMap.get("out_trade_no");
            if (outTradeNo == null || outTradeNo.isEmpty()) {
                log.error("回调通知中缺少订单号");
                return responseFail("缺少订单号");
            }

            log.info("处理订单支付成功回调，订单号：{}", outTradeNo);

            // 调用服务层处理支付成功逻辑
            tradeOrderService.paySuccess(outTradeNo);

            // 返回成功响应给微信支付
            return responseSuccess();

        } catch (Exception e) {
            log.error("处理支付回调失败", e);
            return responseFail("处理失败");
        }
    }

    /**
     * 解析回调通知数据
     * 将微信支付返回的回调数据解析为Map格式
     * 实际项目中应该解析JSON数据并解密
     *
     * @param notifyData 回调通知数据，通常是JSON格式
     * @return 解析后的数据Map，包含订单号等信息
     */
    private Map<String, String> parseNotifyData(String notifyData) {
        // 这里为了简化，返回模拟数据
        // 实际项目中应该使用 JSON.parseObject() 解析数据
        Map<String, String> map = new HashMap<>();

        // 模拟：如果回调数据包含特定标记，则使用模拟订单号
        // 否则尝试从实际数据中解析
        if (notifyData.contains("mock") || notifyData.isEmpty()) {
            // 模拟支付场景，不处理回调
            map.put("out_trade_no", "");
        } else {
            // 实际场景下解析真实数据
            // TODO: 实现JSON解析逻辑
            map.put("out_trade_no", "");
        }

        return map;
    }

    /**
     * 返回成功响应
     * 按照微信支付规范返回JSON格式的成功响应
     * 格式：{"code":"SUCCESS","message":"成功"}

     *
     * @return 成功响应JSON字符串，符合微信支付规范
     */
    private String responseSuccess() {
        Map<String, String> result = new HashMap<>();
        result.put("code", "SUCCESS");
        result.put("message", "成功");
        return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
    }

    /**
     * 返回失败响应
     * 按照微信支付规范返回JSON格式的失败响应
     * 格式：{"code":"FAIL","message":"失败原因"}

     *
     * @param message 失败信息，用于描述失败原因
     * @return 失败响应JSON字符串，符合微信支付规范
     */
    private String responseFail(String message) {
        return "{\"code\":\"FAIL\",\"message\":\"" + message + "\"}";
    }
}
