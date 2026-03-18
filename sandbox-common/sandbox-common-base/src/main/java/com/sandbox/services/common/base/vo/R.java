package com.sandbox.services.common.base.vo;

import com.sandbox.services.common.base.enumeration.ResponseCodeEnum;
import com.yomahub.tlog.context.TLogContext;
import lombok.Getter;
import lombok.Setter;

/**
 * 统一响应对象
 *
 * <p>该类是系统所有接口的统一响应格式，用于封装返回给前端的数据结构。
 * 通过统一的格式，前端可以以一致的方式处理成功和失败的响应，便于接口对接和错误处理。
 *
 * <p><b>响应结构：</b>
 * <ul>
 *   <li><b>code：</b>状态码，0 表示成功，非 0 表示失败（具体含义参考 {@link ResponseCodeEnum}）</li>
 *   <li><b>msg：</b>提示信息，成功时为 "success"，失败时为错误描述</li>
 *   <li><b>data：</b>响应数据，成功时返回业务数据，失败时通常为 null 或错误详情</li>
 *   <li><b>traceId：</b>链路追踪 ID，用于分布式日志追踪，自动从 {@link TLogContext} 获取</li>
 * </ul>
 *
 * <p><b>使用方式：</b>
 * <pre>
 * // 成功响应（带数据）
 * return R.success(userList);
 *
 * // 成功响应（无数据）
 * return R.success();
 *
 * // 失败响应（使用预定义枚举）
 * return R.fail(ResponseCodeEnum.USER_NOT_FOUND, null);
 *
 * // 失败响应（自定义状态码和消息）
 * return R.fail(10001, "用户不存在", null);
 * </pre>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>统一规范：</b>所有接口返回格式统一，便于前端统一处理和错误拦截</li>
 *   <li><b>链路追踪：</b>自动集成 TLog 的 traceId，方便分布式系统的问题排查</li>
 *   <li><b>泛型支持：</b>通过泛型支持任意类型的响应数据，保证类型安全</li>
 *   <li><b>静态工厂方法：</b>提供多种静态方法简化对象创建，提高代码可读性</li>
 *   <li><b>与枚举集成：</b>与 {@link ResponseCodeEnum} 配合使用，规范状态码管理</li>
 * </ul>
 *
 * <p><b>状态码规范：</b>
 * <ul>
 *   <li><b>0：</b>成功</li>
 *   <li><b>正数：</b>业务异常，如 10001（用户不存在）、10002（密码错误）</li>
 *   <li><b>负数：</b>系统异常，如 -1（系统内部错误）、-2（参数校验失败）</li>
 * </ul>
 *
 * @param <T> 响应数据的类型
 * @author 0101
 * @see ResponseCodeEnum
 * @see com.yomahub.tlog.context.TLogContext
 * @since 2026-03-18
 */
@Getter
@Setter
public class R<T> {

    /**
     * 状态码
     *
     * <p>0 表示成功，非 0 表示失败。
     * 失败状态码的具体含义由 {@link ResponseCodeEnum} 定义。
     */
    private int code;

    /**
     * 提示信息
     *
     * <p>成功时通常为 "success" 或自定义成功消息，
     * 失败时为错误描述，可直接展示给前端用户。
     */
    private String msg;

    /**
     * 响应数据
     *
     * <p>成功时返回业务数据，类型由泛型 T 决定。
     * 失败时通常为 null，但也可以返回错误详情对象。
     */
    private T data;

    /**
     * 链路追踪 ID
     *
     * <p>从 TLog 上下文中自动获取，用于在分布式系统中串联一次请求的所有日志。
     * 该字段为 final，对象创建后不可修改。
     */
    private final String traceId = TLogContext.getTraceId();

    /**
     * 无参构造方法
     *
     * <p>用于内部构造，不建议直接使用。
     */
    public R() {
    }

    /**
     * 根据状态码枚举构造响应对象
     *
     * @param responseCodeEnum 状态码枚举，包含 code 和 msg
     */
    public R(ResponseCodeEnum responseCodeEnum) {
        this.setCode(responseCodeEnum.getCode());
        this.setMsg(responseCodeEnum.getDescription());
    }

    /**
     * 根据自定义状态码和消息构造响应对象
     *
     * @param code    自定义状态码
     * @param message 自定义消息
     */
    public R(int code, String message) {
        this.setCode(code);
        this.setMsg(message);
    }

    /**
     * 成功响应（带数据）
     *
     * <p>返回状态码 0，消息 "success"，并携带业务数据。
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应对象
     */
    public static <T> R<T> success(T data) {
        R<T> genericResponse = new R<>(ResponseCodeEnum.SUCCESS);
        genericResponse.setData(data);
        return genericResponse;
    }

    /**
     * 成功响应（无数据）
     *
     * <p>返回状态码 0，消息 "success"，data 为 null。
     *
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> R<T> success() {
        return new R<>(ResponseCodeEnum.SUCCESS);
    }

    /**
     * 失败响应（使用预定义枚举）
     *
     * <p>根据枚举中的状态码和消息返回失败响应。
     *
     * @param responseCode 状态码枚举
     * @param data         错误详情数据（可选）
     * @param <T>          数据类型
     * @return 失败响应对象
     */
    public static <T> R<T> fail(ResponseCodeEnum responseCode, T data) {
        R<T> r = new R<>(responseCode);
        r.setData(data);
        return r;
    }

    /**
     * 失败响应（使用自定义状态码和消息）
     *
     * <p>适用于枚举中未定义的临时错误码或动态错误场景。
     *
     * @param code 自定义状态码
     * @param msg  自定义错误消息
     * @param data 错误详情数据（可选）
     * @param <T>  数据类型
     * @return 失败响应对象
     */
    public static <T> R<T> fail(int code, String msg, T data) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
}