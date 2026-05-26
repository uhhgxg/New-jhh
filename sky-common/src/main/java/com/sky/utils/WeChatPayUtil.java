package com.sky.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.properties.WeChatProperties;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * 微信支付工具类
 */
@Component
public class WeChatPayUtil {

    //微信支付下单接口地址
    public static final String JSAPI = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";

    //申请退款接口地址
    public static final String REFUNDS = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds";

    @Autowired
    private WeChatProperties weChatProperties;

    /**
     * 获取调用微信接口的客户端工具对象
     *
     * @return 返回一个CloseableHttpClient对象，用于调用微信支付接口，如果加载证书失败则返回null
     */
    private CloseableHttpClient getClient() {
        PrivateKey merchantPrivateKey = null;  // 商户API私钥对象，用于签名请求
        try {
            //merchantPrivateKey商户API私钥，如何加载商户API私钥请看常见问题
            merchantPrivateKey = PemUtil.loadPrivateKey(new FileInputStream(new File(weChatProperties.getPrivateKeyFilePath())));
            //加载平台证书文件
            X509Certificate x509Certificate = PemUtil.loadCertificate(new FileInputStream(new File(weChatProperties.getWeChatPayCertFilePath())));
            //wechatPayCertificates微信支付平台证书列表。你也可以使用后面章节提到的“定时更新平台证书功能”，而不需要关心平台证书的来龙去脉
            List<X509Certificate> wechatPayCertificates = Arrays.asList(x509Certificate);

            WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                    .withMerchant(weChatProperties.getMchid(), weChatProperties.getMchSerialNo(), merchantPrivateKey)
                    .withWechatPay(wechatPayCertificates);

            // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
            CloseableHttpClient httpClient = builder.build();
            return httpClient;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 发送post方式请求
     * 该方法用于向指定URL发送POST请求，并返回响应结果

     *
     * @param url 请求的目标URL地址
     * @param body 请求体的内容，通常为JSON格式的字符串
     * @return 返回服务器响应的内容，通常为JSON格式的字符串
     * @throws Exception 可能抛出网络请求相关的异常
     */
    private String post(String url, String body) throws Exception {
        // 创建HTTP客户端对象
        CloseableHttpClient httpClient = getClient();

        // 创建POST请求对象
        HttpPost httpPost = new HttpPost(url);
        // 设置请求头，接受JSON格式的响应
        httpPost.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
        // 设置请求内容类型为JSON
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        // 添加微信支付序列号到请求头
        httpPost.addHeader("Wechatpay-Serial", weChatProperties.getMchSerialNo());
        // 设置请求体，并指定UTF-8编码
        httpPost.setEntity(new StringEntity(body, "UTF-8"));

        // 执行请求并获取响应
        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            // 将响应实体转换为字符串
            String bodyAsString = EntityUtils.toString(response.getEntity());
            return bodyAsString;
        } finally {
            // 确保关闭HTTP客户端和响应对象，释放资源
            httpClient.close();
            response.close();
        }
    }

    /**
     * 发送get方式请求
     *
     * @param url
     * @return
     */
    private String get(String url) throws Exception {
        CloseableHttpClient httpClient = getClient();

        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
        httpGet.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        httpGet.addHeader("Wechatpay-Serial", weChatProperties.getMchSerialNo());

        CloseableHttpResponse response = httpClient.execute(httpGet);
        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            return bodyAsString;
        } finally {
            httpClient.close();
            response.close();
        }
    }

    /**
     * jsapi下单方法
     * 用于创建微信JSAPI支付订单

 *
     * @param orderNum    商户订单号，用于唯一标识商户的订单
     * @param total       总金额，单位为元，会自动转换为分为单位
     * @param description 商品描述，用于支付页面的显示
     * @param openid      微信用户的openid，用于标识支付用户
     * @return 返回微信支付API的响应结果
 * @throws Exception 可能抛出异常，如网络请求失败、参数错误等
     */
    private String jsapi(String orderNum, BigDecimal total, String description, String openid) throws Exception {
    // 创建JSON对象用于构建请求参数
        JSONObject jsonObject = new JSONObject();
    // 添加应用ID和商户ID
        jsonObject.put("appid", weChatProperties.getAppid());
        jsonObject.put("mchid", weChatProperties.getMchid());
    // 添加商品描述和商户订单号
        jsonObject.put("description", description);
        jsonObject.put("out_trade_no", orderNum);
    // 添加回调通知URL
        jsonObject.put("notify_url", weChatProperties.getNotifyUrl());

    // 创建金额对象，将元转换为分
        JSONObject amount = new JSONObject();
    // 金额需要转换为分为单位，并四舍五入到两位小数
        amount.put("total", total.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).intValue());
    // 设置货币类型为人民币
        amount.put("currency", "CNY");

    // 将金额对象添加到主JSON对象中
        jsonObject.put("amount", amount);

    // 创建支付者对象
        JSONObject payer = new JSONObject();
    // 设置支付者的openid
        payer.put("openid", openid);

    // 将支付者对象添加到主JSON对象中
        jsonObject.put("payer", payer);

    // 将JSON对象转换为字符串作为请求体
        String body = jsonObject.toJSONString();
    // 发送POST请求到微信支付API并返回结果
        return post(JSAPI, body);
    }

    /**
     * 小程序支付
     *
     * @param orderNum    商户订单号
     * @param total       金额，单位 元
     * @param description 商品描述
     * @param openid      微信用户的openid
     * @return 返回包含支付所需参数的JSONObject
     */
    public JSONObject pay(String orderNum, BigDecimal total, String description, String openid) throws Exception {
        //统一下单，生成预支付交易单
        String bodyAsString = jsapi(orderNum, total, description, openid);
        //解析返回结果
        JSONObject jsonObject = JSON.parseObject(bodyAsString);
        System.out.println(jsonObject);

    // 获取预支付ID
        String prepayId = jsonObject.getString("prepay_id");
        if (prepayId != null) {
        // 生成时间戳
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        // 生成随机字符串
            String nonceStr = RandomStringUtils.randomNumeric(32);
        // 创建列表用于签名
            ArrayList<Object> list = new ArrayList<>();
            list.add(weChatProperties.getAppid());
            list.add(timeStamp);
            list.add(nonceStr);
            list.add("prepay_id=" + prepayId);
            //二次签名，调起支付需要重新签名
        // 构建签名字符串
            StringBuilder stringBuilder = new StringBuilder();
            for (Object o : list) {
                stringBuilder.append(o).append("\n");
            }
            String signMessage = stringBuilder.toString();
            byte[] message = signMessage.getBytes();

        // 初始化签名对象
            Signature signature = Signature.getInstance("SHA256withRSA");
        // 加载私钥
            signature.initSign(PemUtil.loadPrivateKey(new FileInputStream(new File(weChatProperties.getPrivateKeyFilePath()))));
        // 更新签名数据
            signature.update(message);
        // 生成签名
            String packageSign = Base64.getEncoder().encodeToString(signature.sign());

            //构造数据给微信小程序，用于调起微信支付
            JSONObject jo = new JSONObject();
        // 添加时间戳
            jo.put("timeStamp", timeStamp);
        // 添加随机字符串
            jo.put("nonceStr", nonceStr);
        // 添加预支付ID
            jo.put("package", "prepay_id=" + prepayId);
        // 添加签名类型
            jo.put("signType", "RSA");
        // 添加签名
            jo.put("paySign", packageSign);

            return jo;
        }
        return jsonObject;
    }

    /**
     * 申请退款
     *
     * @param outTradeNo    商户订单号
     * @param outRefundNo   商户退款单号
     * @param refund        退款金额
     * @param total         原订单金额
     * @return
     */
    public String refund(String outTradeNo, String outRefundNo, BigDecimal refund, BigDecimal total) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("out_trade_no", outTradeNo);
        jsonObject.put("out_refund_no", outRefundNo);

        JSONObject amount = new JSONObject();
        amount.put("refund", refund.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).intValue());
        amount.put("total", total.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).intValue());
        amount.put("currency", "CNY");

        jsonObject.put("amount", amount);
        jsonObject.put("notify_url", weChatProperties.getRefundNotifyUrl());

        String body = jsonObject.toJSONString();

        //调用申请退款接口
        return post(REFUNDS, body);
    }
}
