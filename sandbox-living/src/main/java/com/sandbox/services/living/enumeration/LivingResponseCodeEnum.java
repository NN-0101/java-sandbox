package com.sandbox.services.living.enumeration;

import lombok.Getter;

/**
 * 业务响应码枚举，定义系统中所有业务相关的返回码和描述信息
 *
 * <p>该枚举集中管理所有业务异常和成功响应的状态码，确保整个系统使用统一的错误码规范。
 * 向前端返回结构化的响应信息。
 *
 * <p><b>设计原则：</b>
 * <ul>
 *   <li>状态码使用整数类型，便于前端解析和判断</li>
 *   <li>描述信息应简洁明了，便于前端直接展示或日志记录</li>
 *   <li>每个状态码应具有唯一性，避免重复定义</li>
 *   <li>状态码按业务模块划分区间，便于维护和扩展</li>
 * </ul>
 *
 * <p><b>状态码区间规划：</b>
 * <ul>
 *   <li><b>10000-19999：</b>AI 模块相关错误码</li>
 *   <li><b>20000-29999：</b>用户认证与授权相关错误码</li>
 *   <li><b>30000-39999：</b>业务数据操作相关错误码</li>
 *   <li><b>40000-49999：</b>系统级错误码（如数据库连接、第三方服务异常）</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>在 Service 层抛出业务异常时，携带对应的错误码</li>
 *   <li>在 Controller 层捕获异常后，将错误码封装到响应体中返回</li>
 *   <li>在 Feign 或 Dubbo 接口中，作为跨服务调用的错误标识</li>
 * </ul>
 *
 * @author 0101
 * @since 2026-03-12
 */
@Getter
public enum LivingResponseCodeEnum {

    AI_CONVERSATION_NOT_EXITS(10000, "会话不存在");

    ;

    private final int code;
    private final String description;

    LivingResponseCodeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

}
