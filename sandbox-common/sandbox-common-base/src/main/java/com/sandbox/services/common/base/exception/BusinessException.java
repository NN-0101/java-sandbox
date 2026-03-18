package com.sandbox.services.common.base.exception;

import com.sandbox.services.common.base.enumeration.ResponseCodeEnum;

/**
 * 业务异常 - 表示业务逻辑处理过程中的异常
 *
 * <p>该异常类继承自 {@link AbstractException}，专门用于业务层面的异常处理。
 * 当业务逻辑执行过程中遇到可预料的错误情况（如参数校验失败、业务规则违反、资源不存在等），
 * 应抛出此异常。通过全局异常处理器 {@link com.sandbox.services.common.base.handler.GlobalExceptionHandler}，
 * 这些异常会被转换为统一的响应格式返回给客户端。
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>参数校验失败：</b>如用户输入不符合要求、格式错误等</li>
 *   <li><b>业务规则违反：</b>如余额不足、库存不够、订单状态不允许操作等</li>
 *   <li><b>资源不存在：</b>如根据 ID 查询用户时用户不存在</li>
 *   <li><b>资源已存在：</b>如注册时手机号已被占用</li>
 *   <li><b>权限不足：</b>如当前用户没有操作权限</li>
 *   <li><b>状态不允许：</b>如已支付的订单不能再次支付</li>
 * </ul>
 *
 * <p><b>设计特点：</b>
 * <ul>
 *   <li><b>继承 {@link AbstractException}：</b>复用基础异常结构，包含 code 和 message 字段</li>
 *   <li><b>与 {@link ResponseCodeEnum} 紧密集成：</b>提供使用枚举的构造方法，统一错误码管理</li>
 *   <li><b>多种构造方式：</b>支持仅枚举、枚举+原始异常、自定义 code+message 等多种构造方式，
 *       适应不同场景下的异常抛出需求</li>
 *   <li><b>访问权限控制：</b>部分构造方法使用 protected，鼓励使用枚举方式创建异常，保持错误码规范性</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 使用枚举构造异常（推荐）
 * if (user == null) {
 *     throw new BusinessException(ResponseCodeEnum.USER_NOT_FOUND);
 * }
 *
 * // 使用枚举 + 原始异常（保留堆栈信息）
 * try {
 *     // 某些操作
 * } catch (Exception e) {
 *     throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR, e);
 * }
 *
 * // 使用自定义 code 和 message（不推荐，除非枚举中未定义）
 * if (amount > balance) {
 *     throw new BusinessException(30001, "余额不足");
 * }
 * </pre>
 *
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>优先使用 {@link ResponseCodeEnum} 中定义的错误码，避免随意使用自定义 code</li>
 *   <li>在 Service 层抛出业务异常时，应使用有意义的错误码和描述，便于前端处理和问题排查</li>
 *   <li>不要使用业务异常传递系统级错误（如数据库连接失败），这些应由其他异常类型处理</li>
 *   <li>抛出异常时建议记录必要的上下文信息，便于后续排查</li>
 * </ul>
 *
 * @author 0101
 * @see AbstractException
 * @see ResponseCodeEnum
 * @see com.sandbox.services.common.base.handler.GlobalExceptionHandler
 * @since 2026-03-12
 */
public class BusinessException extends AbstractException {

    /**
     * 使用业务响应码枚举构造异常
     *
     * <p>从枚举中获取预定义的错误码和描述信息。
     * 这是最常用的构造方式，推荐使用。
     *
     * @param responseCodeEnum 业务响应码枚举
     */
    public BusinessException(ResponseCodeEnum responseCodeEnum) {
        super(responseCodeEnum.getCode(), responseCodeEnum.getDescription());
    }

    /**
     * 使用业务响应码枚举和原始异常构造异常
     *
     * <p>适用于需要保留原始异常堆栈的场景，便于排查问题根源。
     *
     * @param responseCodeEnum 业务响应码枚举
     * @param cause            原始异常（引发当前异常的原因）
     */
    public BusinessException(ResponseCodeEnum responseCodeEnum, Throwable cause) {
        super(responseCodeEnum.getCode(), responseCodeEnum.getDescription(), cause);
    }

    /**
     * 使用自定义错误码和消息构造异常（受保护）
     *
     * <p>该构造方法为 protected，限制外部直接使用，鼓励使用枚举方式。
     * 适用于枚举中未定义的临时或动态错误码场景。
     *
     * @param code    错误码
     * @param message 错误描述
     */
    protected BusinessException(int code, String message) {
        super(code, message);
    }

    /**
     * 使用错误码和原始异常构造异常（受保护）
     *
     * <p>该构造方法为 protected，限制外部直接使用。
     * 适用于需要保留原始异常但使用自定义错误码的场景。
     *
     * @param code  错误码
     * @param cause 原始异常
     */
    protected BusinessException(int code, Throwable cause) {
        super(code, cause);
    }

    /**
     * 使用错误码、自定义消息和原始异常构造异常（受保护）
     *
     * <p>该构造方法为 protected，限制外部直接使用。
     * 提供最完整的异常信息，包括错误码、自定义消息和原始异常。
     *
     * @param code    错误码
     * @param message 错误描述
     * @param cause   原始异常
     */
    protected BusinessException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }
}