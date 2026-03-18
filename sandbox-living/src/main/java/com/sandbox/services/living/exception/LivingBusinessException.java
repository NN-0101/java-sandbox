package com.sandbox.services.living.exception;

import com.sandbox.services.common.base.exception.AbstractException;
import com.sandbox.services.living.enumeration.LivingResponseCodeEnum;

/**
 * Living 模块业务异常 - 表示业务逻辑处理过程中的异常
 *
 * <p>该异常类继承自 {@link AbstractException}，专门用于 Living 模块的业务异常处理。
 * 它封装了业务错误码和错误信息，便于在业务逻辑层抛出可识别的业务异常，
 * 最终由全局异常处理器捕获并转换为标准响应格式返回给前端。
 *
 * <p><b>设计特点：</b>
 * <ul>
 *   <li>与 {@link LivingResponseCodeEnum} 枚举紧密配合，统一管理业务错误码</li>
 *   <li>继承自 {@link AbstractException}，保持与其他模块的异常体系一致</li>
 *   <li>提供多种构造方法，支持不同场景下的异常抛出（仅错误码、错误码+原始异常、自定义消息等）</li>
 *   <li>异常中包含 code 和 message，便于前端识别错误类型并进行相应处理</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>Service 层业务校验失败：</b>如参数校验不通过、数据不存在、状态冲突等</li>
 *   <li><b>业务规则违反：</b>如余额不足、权限不足、操作不允许等</li>
 *   <li><b>外部依赖返回不符合预期的结果：</b>如第三方服务返回失败状态</li>
 * </ul>
 *
 * <p><b>示例用法：</b>
 * <pre>
 * // 使用枚举构造异常
 * if (conversation == null) {
 *     throw new LivingBusinessException(LivingResponseCodeEnum.AI_CONVERSATION_NOT_EXITS);
 * }
 *
 * // 使用自定义消息构造异常
 * if (amount > balance) {
 *     throw new LivingBusinessException(30001, "余额不足，无法完成支付");
 * }
 *
 * // 携带原始异常信息
 * try {
 *     // 某些操作
 * } catch (Exception e) {
 *     throw new LivingBusinessException(LivingResponseCodeEnum.SYSTEM_ERROR, e);
 * }
 * </pre>
 *
 * @author 0101
 * @see LivingResponseCodeEnum
 * @see AbstractException
 * @see com.sandbox.services.common.base.handler.GlobalExceptionHandler
 * @since 2026-03-12
 */
public class LivingBusinessException extends AbstractException {

    /**
     * 使用业务响应码枚举构造异常
     *
     * <p>从枚举中获取预定义的错误码和描述信息。
     *
     * @param responseCodeEnum 业务响应码枚举
     */
    public LivingBusinessException(LivingResponseCodeEnum responseCodeEnum) {
        super(responseCodeEnum.getCode(), responseCodeEnum.getDescription());
    }

    /**
     * 使用业务响应码枚举和原始异常构造异常
     *
     * <p>适用于需要保留原始异常堆栈的场景，便于排查问题。
     *
     * @param responseCodeEnum 业务响应码枚举
     * @param cause            原始异常（引发当前异常的原因）
     */
    public LivingBusinessException(LivingResponseCodeEnum responseCodeEnum, Throwable cause) {
        super(responseCodeEnum.getCode(), responseCodeEnum.getDescription(), cause);
    }

    /**
     * 使用自定义错误码和消息构造异常
     *
     * <p>适用于枚举中未定义的临时或动态错误码场景。
     *
     * @param code    错误码
     * @param message 错误描述
     */
    public LivingBusinessException(int code, String message) {
        super(code, message);
    }

    /**
     * 使用错误码和原始异常构造异常（受保护）
     *
     * <p>该构造方法为 protected，建议优先使用带有描述信息的构造方法，
     * 以保证异常信息的完整性。
     *
     * @param code  错误码
     * @param cause 原始异常
     */
    protected LivingBusinessException(int code, Throwable cause) {
        super(code, cause);
    }

    /**
     * 使用错误码、自定义消息和原始异常构造异常（受保护）
     *
     * <p>提供最完整的异常信息，包括错误码、自定义消息和原始异常。
     *
     * @param code    错误码
     * @param message 错误描述
     * @param cause   原始异常
     */
    protected LivingBusinessException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }
}