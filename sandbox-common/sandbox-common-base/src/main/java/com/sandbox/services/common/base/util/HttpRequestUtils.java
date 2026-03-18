package com.sandbox.services.common.base.util;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSONObject;
import com.sandbox.services.common.base.enumeration.ResponseCodeEnum;
import com.sandbox.services.common.base.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * HTTP 请求工具类
 *
 * <p>该工具类基于 hutool 的 {@link HttpRequest} 封装了常用的 HTTP 请求方法，
 * 支持 GET、POST、表单提交等多种请求方式，并提供了统一的异常处理和日志记录。
 * 所有请求结果统一返回 {@link JSONObject} 类型，便于后续处理。
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>GET 请求：</b>支持带参数、带请求头、自定义超时时间</li>
 *   <li><b>POST 请求：</b>支持 JSON 格式请求体、表单提交、带请求头、自定义超时时间</li>
 *   <li><b>参数拼接：</b>提供 {@link #paramUrl(Map)} 方法将参数拼接为 URL 查询字符串</li>
 *   <li><b>异常处理：</b>请求失败时统一抛出 {@link BusinessException}，包含预定义的 HTTP 请求异常码</li>
 *   <li><b>日志记录：</b>自动记录请求 URL、参数、响应结果，便于问题排查</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>调用第三方 API：</b>如支付接口、短信接口、地图服务等</li>
 *   <li><b>微服务间调用：</b>在未使用 Feign 等 RPC 框架时的简单 HTTP 调用</li>
 *   <li><b>内部服务测试：</b>快速发起 HTTP 请求测试接口</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 简单 GET 请求
 * JSONObject result = HttpRequestUtils.get("<a href="https://api.example.com/users">...</a>");
 *
 * // GET 请求带参数
 * Map&lt;String, Object&gt; params = new HashMap&lt;&gt;();
 * params.put("page", 1);
 * params.put("size", 20);
 * JSONObject result = HttpRequestUtils.get("https://api.example.com/users", params);
 *
 * // POST JSON 请求
 * Map&lt;String, String&gt; headers = new HashMap&lt;&gt;();
 * headers.put("Authorization", "Bearer token");
 * String jsonBody = "{\"name\":\"张三\",\"age\":18}";
 * JSONObject result = HttpRequestUtils.post("https://api.example.com/users", headers, jsonBody);
 *
 * // 表单提交
 * Map&lt;String, Object&gt; formData = new HashMap&lt;&gt;();
 * formData.put("username", "admin");
 * formData.put("password", "123456");
 * JSONObject result = HttpRequestUtils.postForm("https://api.example.com/login", formData);
 * </pre>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>统一返回值：</b>所有方法返回 {@link JSONObject}，便于解析响应数据</li>
 *   <li><b>异常处理：</b>捕获所有异常并转换为业务异常，避免原始异常泄露到上层</li>
 *   <li><b>日志记录：</b>使用 Slf4j 记录请求和响应信息，便于监控和调试</li>
 *   <li><b>超时控制：</b>默认超时时间为 3000 毫秒，支持自定义超时时间</li>
 *   <li><b>方法重载：</b>提供多种参数组合的重载方法，适应不同调用场景</li>
 * </ul>
 *
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>所有方法都会抛出 {@link BusinessException}，调用方需要处理或继续向上抛出</li>
 *   <li>响应结果必须是有效的 JSON 格式，否则解析会失败</li>
 *   <li>对于可能返回非 JSON 格式的接口，应避免使用此工具类或自行扩展</li>
 *   <li>默认超时时间 3 秒可能不适合所有场景，可根据实际需求调整</li>
 *   <li>大量并发请求时，建议使用连接池（Hutool 默认支持）</li>
 * </ul>
 *
 * @author 0101
 * @see HttpRequest
 * @see BusinessException
 * @see ResponseCodeEnum#HTTP_REQUEST_EXCEPTION
 * @since 2026-03-12
 */
@Slf4j
public class HttpRequestUtils {

    /**
     * 默认超时时间（毫秒）
     */
    private static final int DEFAULT_TIMEOUT = 3000;

    /**
     * 私有构造方法，防止实例化
     */
    private HttpRequestUtils() {
        throw new IllegalStateException("Utility class");
    }

    // ==================== POST 请求 ====================

    /**
     * 发送 POST 请求（无参数）
     *
     * @param url 请求 URL
     * @return 响应结果 JSON 对象
     * @throws BusinessException 请求失败时抛出
     */
    public static JSONObject post(String url) {
        try {
            String body = HttpRequest.post(url)
                    .timeout(DEFAULT_TIMEOUT)
                    .execute().body();
            log.info("post request url:{}, result:{}", url, JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /**
     * 发送 POST 请求（带 JSON 参数）
     *
     * @param url    请求 URL
     * @param params 请求参数（自动转换为 JSON）
     * @return 响应结果 JSON 对象
     * @throws BusinessException 请求失败时抛出
     */
    public static JSONObject post(String url, Map<String, Object> params) {
        try {
            String body = HttpRequest.post(url)
                    .body(JSONObject.toJSONString(params))
                    .timeout(DEFAULT_TIMEOUT)
                    .execute().body();
            log.info("post request url:{}, params:{}, result:{}", url, JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /**
     * 发送 POST 请求（带请求头和 JSON 参数）
     *
     * @param url       请求 URL
     * @param headers   请求头
     * @param jsonParam JSON 格式的请求体字符串
     * @return 响应结果 JSON 对象
     * @throws BusinessException 请求失败时抛出
     */
    public static JSONObject post(String url, Map<String, String> headers, String jsonParam) {
        return post(url, headers, jsonParam, DEFAULT_TIMEOUT);
    }

    /**
     * 发送 POST 请求（带请求头、JSON 参数和自定义超时时间）
     *
     * @param url       请求 URL
     * @param headers   请求头
     * @param jsonParam JSON 格式的请求体字符串
     * @param timeout   超时时间（毫秒）
     * @return 响应结果 JSON 对象
     * @throws BusinessException 请求失败时抛出
     */
    public static JSONObject post(String url, Map<String, String> headers, String jsonParam, int timeout) {
        try {
            String body = HttpRequest.post(url)
                    .addHeaders(headers)
                    .body(jsonParam)
                    .timeout(timeout)
                    .execute().body();
            log.info("post request url:{}, headers:{}, params:{}, result:{}",
                    url, JSONObject.toJSONString(headers), jsonParam, JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /**
     * 发送 POST 表单提交请求（带请求头）
     *
     * @param url     请求 URL
     * @param headers 请求头
     * @param params  表单参数
     * @return 响应结果 JSON 对象
     * @throws BusinessException 请求失败时抛出
     */
    public static JSONObject postForm(String url, Map<String, String> headers, Map<String, Object> params) {
        try {
            String body = HttpRequest.post(url)
                    .addHeaders(headers)
                    .form(params)
                    .timeout(DEFAULT_TIMEOUT)
                    .execute().body();
            log.info("post form request url:{}, headers:{}, params:{}, result:{}",
                    url, JSONObject.toJSONString(headers), JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post form request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /**
     * 发送 POST 表单提交请求（无请求头）
     *
     * @param url    请求 URL
     * @param params 表单参数
     * @return 响应结果 JSON 对象
     * @throws BusinessException 请求失败时抛出
     */
    public static JSONObject postForm(String url, Map<String, Object> params) {
        try {
            String body = HttpRequest.post(url)
                    .form(params)
                    .timeout(DEFAULT_TIMEOUT)
                    .execute().body();
            log.info("post form request url:{} ,params:{}, result:{}",
                    url, JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post form request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /**
     * 发送 POST 请求（不抛出异常，失败时返回 null）
     *
     * <p>该方法适用于不需要严格处理失败场景的调用，如消息推送等。
     *
     * @param url       请求 URL
     * @param jsonParam JSON 格式的请求体字符串
     * @return 响应结果 JSON 对象，失败时返回 null
     */
    public static JSONObject sendMessagePost(String url, String jsonParam) {
        try {
            String body = HttpRequest.post(url)
                    .body(jsonParam)
                    .timeout(DEFAULT_TIMEOUT)
                    .execute().body();
            log.info("post request url:{}, params:{}, result:{}", url, jsonParam, JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post request error url:{}, errorMessage:", url, e);
            return null;
        }
    }

    // ==================== GET 请求 ====================

    /**
     * 发送 GET 请求（无参数）
     *
     * @param url 请求 URL
     * @return 响应结果 JSON 对象
     * @throws BusinessException 请求失败时抛出
     */
    public static JSONObject get(String url) {
        try {
            String body = HttpRequest.get(url)
                    .timeout(DEFAULT_TIMEOUT)
                    .execute().body();
            log.info("get request url:{}, result:{}", url, JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("get request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /**
     * 发送 GET 请求（带查询参数）
     *
     * @param url    请求 URL
     * @param params 查询参数
     * @return 响应结果 JSON 对象
     * @throws BusinessException 请求失败时抛出
     */
    public static JSONObject get(String url, Map<String, Object> params) {
        try {
            String body = HttpRequest.get(url)
                    .form(params)
                    .timeout(DEFAULT_TIMEOUT)
                    .execute().body();
            log.info("get request url:{}, params:{}, result:{}",
                    url, JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("get request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /**
     * 发送 GET 请求（带请求头和查询参数）
     *
     * @param url     请求 URL
     * @param headers 请求头
     * @param params  查询参数
     * @return 响应结果 JSON 对象
     * @throws BusinessException 请求失败时抛出
     */
    public static JSONObject get(String url, Map<String, String> headers, Map<String, Object> params) {
        return get(url, headers, params, DEFAULT_TIMEOUT);
    }

    /**
     * 发送 GET 请求（带请求头、查询参数和自定义超时时间）
     *
     * @param url     请求 URL
     * @param headers 请求头
     * @param params  查询参数
     * @param timeout 超时时间（毫秒）
     * @return 响应结果 JSON 对象
     * @throws BusinessException 请求失败时抛出
     */
    public static JSONObject get(String url, Map<String, String> headers, Map<String, Object> params, int timeout) {
        try {
            String body = HttpRequest.get(url)
                    .addHeaders(headers)
                    .form(params)
                    .timeout(timeout)
                    .execute().body();
            log.info("get request url:{}, headers:{}, params:{}, result:{}",
                    url, JSONObject.toJSONString(headers), JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("get request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /**
     * 发送 GET 请求（参数直接拼接到 URL 中）
     *
     * <p>适用于参数需要作为 URL 路径一部分的场景。
     *
     * @param url     请求基础 URL
     * @param headers 请求头
     * @param params  查询参数
     * @return 响应结果 JSON 对象
     * @throws BusinessException 请求失败时抛出
     */
    public static JSONObject getParamUrl(String url, Map<String, String> headers, Map<String, Object> params) {
        try {
            String result = HttpRequest.get(url + paramUrl(params))
                    .addHeaders(headers)
                    .timeout(DEFAULT_TIMEOUT)
                    .execute().body();
            log.info("get request url:{}, headers:{}, params:{}, result:{}",
                    url, JSONObject.toJSONString(headers), JSONObject.toJSONString(params), JSONObject.toJSONString(result));
            return JSONObject.parseObject(result);
        } catch (Exception e) {
            log.error("get request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /**
     * 将参数 Map 拼接为 URL 查询字符串
     *
     * <p>例如：{"page":1, "size":20} 转换为 "?page=1&size=20"
     *
     * @param param 参数 Map
     * @return 拼接好的查询字符串，以 "?" 开头
     */
    public static String paramUrl(Map<String, Object> param) {
        if (param == null || param.isEmpty()) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("?");

        // 遍历参数并拼接
        for (Map.Entry<String, Object> map : param.entrySet()) {
            stringBuilder.append(map.getKey()).append("=").append(map.getValue());
            stringBuilder.append("&");
        }

        // 去除最后一个 "&"
        String path = stringBuilder.toString();
        return path.substring(0, path.length() - 1);
    }
}