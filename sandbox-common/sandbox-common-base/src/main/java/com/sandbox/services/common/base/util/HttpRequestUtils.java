package com.sandbox.services.common.base.util;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSONObject;
import com.sandbox.services.common.base.enumeration.ResponseCodeEnum;
import com.sandbox.services.common.base.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @description: http请求帮助类
 * @author: 0101
 * @create: 2026/3/12
 */
@Slf4j
public class HttpRequestUtils {

    /**
     * POST请求
     *
     * @param url 请求url
     * @return 请求结果
     */
    public static JSONObject post(String url) {
        try {
            String body = HttpRequest.post(url)
                    .timeout(3000)
                    .execute().body();
            log.info("post request url:{}, result:{}", url, JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /**
     * POST请求
     *
     * @param url    请求url
     * @param params 请求参数
     * @return 请求结果
     */
    public static JSONObject post(String url, Map<String, Object> params) {
        try {
            String body = HttpRequest.post(url)
                    .body(JSONObject.toJSONString(params))
                    .timeout(3000)
                    .execute().body();
            log.info("post request url:{}, params:{}, result:{}", url, JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /**
     * POST请求
     *
     * @param url       请求url
     * @param headers   请求头
     * @param jsonParam 请求参数
     * @return 请求结果
     */
    public static JSONObject post(String url, Map<String, String> headers, String jsonParam) {
        try {
            String body = HttpRequest.post(url)
                    .addHeaders(headers)
                    .body(jsonParam)
                    .timeout(3000)
                    .execute().body();
            log.info("post request url:{}, headers:{},params:{}, result:{}", url, JSONObject.toJSONString(headers),
                    jsonParam, JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /**
     * POST请求
     *
     * @param url       请求url
     * @param headers   请求头
     * @param jsonParam 请求参数
     * @param timeout   超时时间 毫秒
     * @return 请求结果
     */
    public static JSONObject post(String url, Map<String, String> headers, String jsonParam, int timeout) {
        try {
            String body = HttpRequest.post(url)
                    .addHeaders(headers)
                    .body(jsonParam)
                    .timeout(timeout)
                    .execute().body();
            log.info("post request url:{}, headers:{},params:{}, result:{}", url, JSONObject.toJSONString(headers),
                    jsonParam, JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /**
     * post 表单提交请求
     *
     * @param url     请求url
     * @param headers 请求头
     * @param params  请求参数
     * @return 请求结果
     */
    public static JSONObject postForm(String url, Map<String, String> headers, Map<String, Object> params) {
        try {
            String body = HttpRequest.post(url)
                    .addHeaders(headers)
                    .form(params)
                    .timeout(3000)
                    .execute().body();
            log.info("post request url:{}, headers:{},params:{}, result:{}", url, JSONObject.toJSONString(headers),
                    JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /**
     * post 表单提交请求
     *
     * @param url    请求url
     * @param params 请求参数
     * @return 请求结果
     */
    public static JSONObject postForm(String url, Map<String, Object> params) {
        try {
            String body = HttpRequest.post(url)
                    .form(params)
                    .timeout(3000)
                    .execute().body();
            log.info("post request url:{} ,params:{}, result:{}", url, JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /**
     * GET请求
     *
     * @param url 请求url
     * @return 请求结果
     */
    public static JSONObject get(String url) {
        try {
            String body = HttpRequest.get(url)
                    .timeout(3000)
                    .execute().body();
            log.info("get request url:{}, result:{}", url, JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("get request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /**
     * GET请求
     *
     * @param url    请求url
     * @param params 参数
     * @return 请求结果
     */
    public static JSONObject get(String url, Map<String, Object> params) {
        try {
            String body = HttpRequest.get(url)
                    .form(params)
                    .timeout(3000)
                    .execute().body();
            log.info("get request url:{}, params:{}, result:{}", url, JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("get request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /**
     * GET请求
     *
     * @param url     请求url
     * @param params  参数
     * @param headers 请求头
     * @return 请求结果
     */
    public static JSONObject get(String url, Map<String, String> headers, Map<String, Object> params) {
        try {
            String body = HttpRequest.get(url)
                    .addHeaders(headers)
                    .form(params)
                    .timeout(3000)
                    .execute().body();
            log.info("get request url:{}, headers:{},params:{}, result:{}", url, JSONObject.toJSONString(headers),
                    JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("get request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /**
     * GET请求
     *
     * @param url     请求url
     * @param params  参数
     * @param headers 请求头
     * @param timeout 毫秒超时时间
     * @return 请求结果
     */
    public static JSONObject get(String url, Map<String, String> headers, Map<String, Object> params, int timeout) {
        try {
            String body = HttpRequest.get(url)
                    .addHeaders(headers)
                    .form(params)
                    .timeout(timeout)
                    .execute().body();
            log.info("get request url:{}, headers:{},params:{}, result:{}", url, JSONObject.toJSONString(headers),
                    JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("get request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /**
     * GET请求
     *
     * @param url     请求url
     * @param params  参数
     * @param headers 请求头
     * @return 请求结果
     */
    public static JSONObject getParamUrl(String url, Map<String, String> headers, Map<String, Object> params) {
        try {
            String result = HttpRequest.get(url + paramUrl(params))
                    .addHeaders(headers)
                    .timeout(3000)
                    .execute().body();
            log.info("get request url:{}, headers:{},params:{}, result:{}", url, JSONObject.toJSONString(headers),
                    JSONObject.toJSONString(params), JSONObject.toJSONString(result));
            return JSONObject.parseObject(result);
        } catch (Exception e) {
            log.error("get request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /**
     * POST请求
     *
     * @param url       请求url
     * @param jsonParam 请求参数
     * @return 请求结果
     */
    public static JSONObject sendMessagePost(String url, String jsonParam) {
        try {
            String body = HttpRequest.post(url)
                    .body(jsonParam)
                    .timeout(3000)
                    .execute().body();
            log.info("post request url:{},params:{}, result:{}", url, jsonParam, JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post request error url:{}, errorMessage:", url, e);
            return null;
        }
    }

    /**
     * 路径参数拼接
     *
     * @param param 参数
     * @return 参数url
     */
    public static String paramUrl(Map<String, Object> param) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("?");
        //拼接URL
        for (Map.Entry<String, Object> map : param.entrySet()) {
            stringBuilder.append(map.getKey()).append("=").append(map.getValue());
            stringBuilder.append("&");
        }
        String path = stringBuilder.toString();
        return path.substring(0, path.length() - 1);
    }
}
