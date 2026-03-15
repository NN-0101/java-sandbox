package com.sandbox.services.common.base.enumeration;

import lombok.Getter;

/**
 * @description:
 * @author: 0101
 * @create: 2026/3/12
 */
@Getter
public enum ResponseCodeEnum {

    SUCCESS(0, "success"),

    SYSTEM_ERROR(999999, "系统异常"),

    INFRASTRUCTURE_ERROR(100001, "基础服务异常，请联系管理员"),

    SQL_ERROR(100002, "数据处理异常，请联系管理员"),

    PARAMETER_ERROR(100003, "参数错误"),

    TIMEOUT_EXCEPTION(100004, "调用超时"),

    ASYNC_SERVICE_EXCEPTION(100005, "服务异常"),

    SERVICE_EXCEPTION(100006, "服务异常"),

    HTTP_REQUEST_EXCEPTION(100007, "HTTP调用异常"),

    JSON_PARSER_EXCEPTION(100008, "JSON解析异常");

    private final int code;
    private final String description;

    ResponseCodeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }


}
