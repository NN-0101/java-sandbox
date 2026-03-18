package com.sandbox.services.common.base.enumeration;

import lombok.Getter;

/**
 * 全局响应码枚举
 *
 * <p>该枚举定义了系统中所有标准化的响应码及其对应的描述信息，用于统一管理
 * 成功和失败的返回状态。通过与 {@link com.sandbox.services.common.base.vo.R} 配合使用，
 * 确保所有接口返回的状态码都具有明确的业务含义，便于前端统一处理和错误识别。
 *
 * <p><b>响应码设计规范：</b>
 * <ul>
 *   <li><b>0：</b>表示成功</li>
 *   <li><b>100000-199999：</b>系统级异常（系统错误、基础设施错误、SQL错误等）</li>
 *   <li><b>其他正数：</b>业务相关错误码，由各业务模块自行定义（本枚举暂未包含）</li>
 * </ul>
 *
 * <p><b>响应码说明：</b>
 * <table border="1">
 *   <tr>
 *     <th>响应码</th>
 *     <th>枚举项</th>
 *     <th>描述</th>
 *     <th>说明</th>
 *   </tr>
 *   <tr>
 *     <td>0</td>
 *     <td>{@link #SUCCESS}</td>
 *     <td>success</td>
 *     <td>请求处理成功，正常返回数据</td>
 *   </tr>
 *   <tr>
 *     <td>999999</td>
 *     <td>{@link #SYSTEM_ERROR}</td>
 *     <td>系统异常</td>
 *     <td>未预期的系统级错误，需联系管理员</td>
 *   </tr>
 *   <tr>
 *     <td>100001</td>
 *     <td>{@link #INFRASTRUCTURE_ERROR}</td>
 *     <td>基础服务异常，请联系管理员</td>
 *     <td>基础设施（如缓存、消息队列）相关错误</td>
 *   </tr>
 *   <tr>
 *     <td>100002</td>
 *     <td>{@link #SQL_ERROR}</td>
 *     <td>数据处理异常，请联系管理员</td>
 *     <td>数据库操作相关错误</td>
 *   </tr>
 *   <tr>
 *     <td>100003</td>
 *     <td>{@link #PARAMETER_ERROR}</td>
 *     <td>参数错误</td>
 *     <td>请求参数校验失败</td>
 *   </tr>
 *   <tr>
 *     <td>100004</td>
 *     <td>{@link #TIMEOUT_EXCEPTION}</td>
 *     <td>调用超时</td>
 *     <td>远程调用或异步操作超时</td>
 *   </tr>
 *   <tr>
 *     <td>100005</td>
 *     <td>{@link #ASYNC_SERVICE_EXCEPTION}</td>
 *     <td>服务异常</td>
 *     <td>异步服务调用异常</td>
 *   </tr>
 *   <tr>
 *     <td>100006</td>
 *     <td>{@link #SERVICE_EXCEPTION}</td>
 *     <td>服务异常</td>
 *     <td>通用服务层异常</td>
 *   </tr>
 *   <tr>
 *     <td>100007</td>
 *     <td>{@link #HTTP_REQUEST_EXCEPTION}</td>
 *     <td>HTTP调用异常</td>
 *     <td>HTTP 请求失败或响应异常</td>
 *   </tr>
 *   <tr>
 *     <td>100008</td>
 *     <td>{@link #JSON_PARSER_EXCEPTION}</td>
 *     <td>JSON解析异常</td>
 *     <td>JSON 序列化或反序列化失败</td>
 *   </tr>
 * </table>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>统一响应：</b>在 {@link com.sandbox.services.common.base.vo.R} 中作为成功或失败的响应码</li>
 *   <li><b>业务异常：</b>在 {@link com.sandbox.services.common.base.exception.BusinessException} 中作为错误码</li>
 *   <li><b>全局异常处理：</b>在 {@link com.sandbox.services.common.base.handler.GlobalExceptionHandler} 中根据异常类型返回对应的响应码</li>
 *   <li><b>接口文档：</b>用于生成接口文档中的响应码说明</li>
 * </ul>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>集中管理：</b>所有响应码集中在一个枚举中，便于维护和查找</li>
 *   <li><b>可读性强：</b>枚举项名称采用大写和下划线，含义清晰</li>
 *   <li><b>扩展性：</b>可根据业务需要继续添加新的响应码，保持与现有码值不冲突</li>
 *   <li><b>与前端约定：</b>响应码 0 表示成功，非 0 表示失败，前端可根据此约定进行全局错误处理</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 在 R 中使用
 * public static <T> R<T> success(T data) {
 *     return new R<>(ResponseCodeEnum.SUCCESS, data);
 * }
 *
 * // 在 BusinessException 中使用
 * throw new BusinessException(ResponseCodeEnum.USER_NOT_FOUND);
 *
 * // 在全局异常处理器中使用
 * return new R<>(ResponseCodeEnum.PARAMETER_ERROR.getCode(), errorMsg);
 * </pre>
 *
 * @author 0101
 * @see com.sandbox.services.common.base.vo.R
 * @see com.sandbox.services.common.base.exception.BusinessException
 * @see com.sandbox.services.common.base.handler.GlobalExceptionHandler
 * @since 2026-03-12
 */
@Getter
public enum ResponseCodeEnum {

    /**
     * 成功
     *
     * <p>请求处理成功，正常返回数据
     */
    SUCCESS(0, "success"),

    /**
     * 系统异常
     *
     * <p>未预期的系统级错误，需联系管理员
     */
    SYSTEM_ERROR(999999, "系统异常"),

    /**
     * 基础服务异常
     *
     * <p>基础设施（如缓存、消息队列）相关错误
     */
    INFRASTRUCTURE_ERROR(100001, "基础服务异常，请联系管理员"),

    /**
     * SQL 异常
     *
     * <p>数据库操作相关错误（如 SQL 语法错误、约束违反等）
     */
    SQL_ERROR(100002, "数据处理异常，请联系管理员"),

    /**
     * 参数错误
     *
     * <p>请求参数校验失败（如格式错误、必填项缺失、业务规则违反等）
     */
    PARAMETER_ERROR(100003, "参数错误"),

    /**
     * 调用超时
     *
     * <p>远程调用或异步操作超时
     */
    TIMEOUT_EXCEPTION(100004, "调用超时"),

    /**
     * 异步服务异常
     *
     * <p>异步服务调用过程中发生异常
     */
    ASYNC_SERVICE_EXCEPTION(100005, "服务异常"),

    /**
     * 服务异常
     *
     * <p>通用服务层异常，适用于未具体分类的服务错误
     */
    SERVICE_EXCEPTION(100006, "服务异常"),

    /**
     * HTTP 调用异常
     *
     * <p>HTTP 请求失败或响应异常（如连接失败、响应状态码非 200 等）
     */
    HTTP_REQUEST_EXCEPTION(100007, "HTTP调用异常"),

    /**
     * JSON 解析异常
     *
     * <p>JSON 序列化或反序列化失败（如格式错误、类型不匹配等）
     */
    JSON_PARSER_EXCEPTION(100008, "JSON解析异常");

    /**
     * 响应码
     *
     * <p>整数类型状态码，0 表示成功，非 0 表示失败
     */
    private final int code;

    /**
     * 响应描述
     *
     * <p>对响应码的文字说明，可用于日志记录或前端展示
     */
    private final String description;

    /**
     * 构造方法
     *
     * @param code        响应码
     * @param description 响应描述
     */
    ResponseCodeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }
}