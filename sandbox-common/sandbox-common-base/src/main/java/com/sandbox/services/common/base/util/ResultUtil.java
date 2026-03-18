package com.sandbox.services.common.base.util;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;

import java.util.Objects;

/**
 * 接口响应结果处理工具类
 *
 * <p>该工具类用于处理 HTTP 接口调用的响应结果，提供了一系列便捷方法
 * 用于判断请求是否成功、提取响应数据、以及将响应数据转换为目标对象。
 * 通常与 {@link HttpRequestUtils} 配合使用，完成完整的 HTTP 调用流程。
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>成功判断：</b>{@link #isSuccess(JSONObject)} 判断响应是否成功（code 为 "0"）</li>
 *   <li><b>数据提取：</b>{@link #getJsonData(JSONObject)} 提取响应中的 data 字段（JSON 格式）</li>
 *   <li><b>对象转换：</b>{@link #getDataObject(JSONObject, Class)} 将响应数据转换为指定类型的 Java 对象</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>内部服务调用：</b>处理微服务间调用的响应结果</li>
 *   <li><b>第三方 API 调用：</b>解析统一格式的 API 响应</li>
 *   <li><b>接口测试：</b>验证接口返回结果</li>
 * </ul>
 *
 * <p><b>响应格式约定：</b>
 * 该工具类假设所有接口的响应格式统一为：
 * <pre>
 * {
 *   "code": "0",        // 状态码，"0" 表示成功，非 "0" 表示失败
 *   "msg": "success",   // 提示信息
 *   "data": { ... }     // 响应数据（可以是对象或数组）
 * }
 * </pre>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 1. 发起 HTTP 请求
 * JSONObject response = HttpRequestUtils.get("http://api.example.com/users");
 *
 * // 2. 判断是否成功
 * if (ResultUtil.isSuccess(response)) {
 *     // 3. 获取 data 字段（JSON 格式）
 *     JSONObject data = ResultUtil.getJsonData(response);
 *
 *     // 4. 转换为目标对象
 *     UserDTO user = ResultUtil.getDataObject(response, UserDTO.class);
 *
 *     // 5. 使用数据
 *     System.out.println(user.getName());
 * }
 * </pre>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>空安全：</b>所有方法都进行了空值检查，避免 NullPointerException</li>
 *   <li><b>单一职责：</b>每个方法只负责一个功能，易于理解和维护</li>
 *   <li><b>与 Hutool 集成：</b>使用 {@link BeanUtil#copyProperties} 进行对象属性拷贝</li>
 *   <li><b>约定大于配置：</b>约定响应格式，简化调用方代码</li>
 * </ul>
 *
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>该工具类要求响应中的 code 字段为字符串类型，且成功时为 "0"</li>
 *   <li>如果实际接口的响应格式不同，需要修改此工具类或创建新的适配器</li>
 *   <li>data 字段可能为 JSON 对象、JSON 数组或普通字符串，调用时需注意类型匹配</li>
 *   <li>{@link #getDataObject(JSONObject, Class)} 方法只能处理 data 为对象的场景，
 *       如果 data 是数组，需要使用 {@link BeanUtil#copyToList} 或其他方法</li>
 * </ul>
 *
 * @author 0101
 * @see HttpRequestUtils
 * @see cn.hutool.core.bean.BeanUtil
 * @since 2026-03-12
 */
public class ResultUtil {

    /**
     * 私有构造方法，防止实例化
     */
    private ResultUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 判断内部接口调用是否成功
     *
     * <p>根据响应中的 code 字段判断，如果 code 为 "0" 则认为成功。
     *
     * @param result HTTP 响应结果 JSON 对象
     * @return true: 调用成功；false: 调用失败或响应为 null
     */
    public static boolean isSuccess(JSONObject result) {
        return result != null && Objects.equals(result.getString("code"), "0");
    }

    /**
     * 获取内部接口响应的 data 字段（JSON 对象格式）
     *
     * <p>如果调用成功，返回响应中的 data 字段（JSON 对象）；否则返回 null。
     *
     * @param result HTTP 响应结果 JSON 对象
     * @return data 字段的 JSON 对象，失败或不存在时返回 null
     */
    public static JSONObject getJsonData(JSONObject result) {
        if (isSuccess(result)) {
            return result.getJSONObject("data");
        }
        return null;
    }

    /**
     * 将响应内容转换为指定类型的 Java 对象
     *
     * <p>如果调用成功，将整个响应对象（包含 code、msg、data）转换为目标类型。
     * 注意：此方法转换的是整个响应对象，而不仅仅是 data 字段。
     *
     * @param result HTTP 响应结果 JSON 对象
     * @param clazz  目标对象类型
     * @param <T>    泛型类型
     * @return 转换后的目标对象，失败时返回 null
     */
    public static <T> T getDataObject(JSONObject result, Class<T> clazz) {
        if (isSuccess(result)) {
            // 注意：这里转换的是整个 result 对象，而不是 result.getJSONObject("data")
            // 如果只需要转换 data 字段，需要先获取 data 再转换
            return BeanUtil.copyProperties(result, clazz);
        }
        return null;
    }

    /**
     * 获取 data 字段并转换为指定类型的 Java 对象
     *
     * <p>此方法是 {@link #getDataObject(JSONObject, Class)} 的补充，
     * 专门用于将 data 字段转换为目标对象。
     *
     * @param result HTTP 响应结果 JSON 对象
     * @param clazz  目标对象类型
     * @param <T>    泛型类型
     * @return 转换后的目标对象，失败时返回 null
     */
    public static <T> T getDataFieldObject(JSONObject result, Class<T> clazz) {
        JSONObject data = getJsonData(result);
        if (data != null) {
            return BeanUtil.copyProperties(data, clazz);
        }
        return null;
    }
}