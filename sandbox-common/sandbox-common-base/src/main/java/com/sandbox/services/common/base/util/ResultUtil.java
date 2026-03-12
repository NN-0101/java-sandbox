package com.sandbox.services.common.base.util;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;

import java.util.Objects;

/**
 * @description: 接口响应帮助类
 * @author: 0101
 * @create: 2026/3/12
 */
public class ResultUtil {

    /**
     * 内部接口是否调用成功
     *
     * @param result 相应结果
     * @return 是否成功
     */
    public static boolean isSuccess(JSONObject result) {
        return result != null && Objects.equals(result.getString("code"), "0");
    }

    /**
     * 内部调用接口 获取响应内容字符串
     *
     * @param result 相应结果
     * @return 响应内容字符串
     */
    public static JSONObject getJsonData(JSONObject result) {
        if (isSuccess(result)) {
            return result.getJSONObject("data");
        }
        return null;
    }

    /**
     * 将响应内容转换成想要的对象
     *
     * @param result 相应内容
     * @param clazz  目标对象类型
     * @param <T>    泛型
     * @return 结果
     */
    public static <T> T getDataObject(JSONObject result, Class<T> clazz) {
        if (isSuccess(result)) {
            return BeanUtil.copyProperties(result, clazz);
        }
        return null;
    }
}
