package com.sky.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Http工具类
 * 提供了发送HTTP GET和POST请求的方法，支持表单和JSON格式的请求
 */
public class HttpClientUtil {

    // 定义请求超时时间，单位为毫秒
    static final  int TIMEOUT_MSEC = 5 * 1000;

    /**
     * 发送GET方式请求
     * @param url 请求地址
     * @param paramMap 请求参数，key为参数名，value为参数值
     * @return 返回服务器响应的字符串数据
     */
    public static String doGet(String url,Map<String,String> paramMap){
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String result = "";
        CloseableHttpResponse response = null;

        try{
            // 构建URI，将参数添加到URL中
            URIBuilder builder = new URIBuilder(url);
            if(paramMap != null){
                for (String key : paramMap.keySet()) {
                    builder.addParameter(key,paramMap.get(key));
                }
            }
            URI uri = builder.build();

            //创建GET请求
            HttpGet httpGet = new HttpGet(uri);

            //发送请求
            response = httpClient.execute(httpGet);

            //判断响应状态
            if(response.getStatusLine().getStatusCode() == 200){
                result = EntityUtils.toString(response.getEntity(),"UTF-8");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭响应和客户端对象
            try {
                response.close();
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 发送POST方式请求（表单格式）
     * @param url 请求地址
     * @param paramMap 请求参数，key为参数名，value为参数值
     * @return 返回服务器响应的字符串数据
     * @throws IOException 可能抛出IO异常
     */
    public static String doPost(String url, Map<String, String> paramMap) throws IOException {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            // 创建参数列表
            if (paramMap != null) {
                List<NameValuePair> paramList = new ArrayList();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    paramList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
                }
                // 模拟表单数据
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
                httpPost.setEntity(entity);
            }

            // 设置请求配置
            httpPost.setConfig(builderRequestConfig());

            // 执行http请求
            response = httpClient.execute(httpPost);

            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }

    /**
     * 发送POST方式请求（JSON格式）
     * @param url 请求地址
     * @param paramMap 请求参数，key为参数名，value为参数值
     * @return 返回服务器响应的字符串数据
     * @throws IOException 可能抛出IO异常
     */
    public static String doPost4Json(String url, Map<String, String> paramMap) throws IOException {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            if (paramMap != null) {
                //构造json格式数据
                JSONObject jsonObject = new JSONObject();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    jsonObject.put(param.getKey(),param.getValue());
                }
                // 创建JSON格式的请求体
                StringEntity entity = new StringEntity(jsonObject.toString(),"utf-8");
                //设置请求编码
                entity.setContentEncoding("utf-8");
                //设置数据类型为JSON
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }

            // 设置请求配置
            httpPost.setConfig(builderRequestConfig());

            // 执行http请求
            response = httpClient.execute(httpPost);

            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }
    /**
     * 发送POST方式请求（JSON格式，支持自定义请求头）
     * @param url 请求地址
     * @param jsonBody JSON格式的请求体字符串
     * @param headers 自定义请求头，key为头名称，value为头值
     * @return 返回服务器响应的字符串数据
     * @throws IOException 可能抛出IO异常
     */
    public static String doPostJsonWithHeaders(String url, String jsonBody, Map<String, String> headers) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            HttpPost httpPost = new HttpPost(url);

            if (jsonBody != null) {
                StringEntity entity = new StringEntity(jsonBody, "utf-8");
                entity.setContentEncoding("utf-8");
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }

            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }

            httpPost.setConfig(builderRequestConfig(60 * 1000));

            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }

    /**
     * 构建请求配置对象
     * @return 返回配置好的RequestConfig对象
     */
    private static RequestConfig builderRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MSEC)    // 连接超时时间
                .setConnectionRequestTimeout(TIMEOUT_MSEC)  // 请求超时时间
                .setSocketTimeout(TIMEOUT_MSEC).build();
    }

    /**
     * 构建请求配置对象（自定义超时时间）
     * @param timeoutMsec 超时时间（毫秒）
     * @return 返回配置好的RequestConfig对象
     */
    private static RequestConfig builderRequestConfig(int timeoutMsec) {
        return RequestConfig.custom()
                .setConnectTimeout(timeoutMsec)
                .setConnectionRequestTimeout(timeoutMsec)
                .setSocketTimeout(timeoutMsec).build();
    }

}
