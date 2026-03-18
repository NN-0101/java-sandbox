package com.sandbox.services.common.base.exception;

import lombok.Getter;

import java.io.Serial;

/**
 * 抽象业务异常 - 所有业务异常的基类
 *
 * <p>该类是所有自定义业务异常的抽象基类，继承自 {@link RuntimeException}，
 * 为系统中的业务异常提供了统一的继承体系和基本结构。通过定义包含错误码的异常模型，
 * 使得异常信息更加规范化，便于前端处理和问题排查。
 *
 * <p><b>核心特性：</b>
 * <ul>
 *   <li><b>统一异常结构：</b>所有业务异常都包含 {@code code}（错误码）和 {@code message}（错误信息），
 *       保持异常信息的一致性</li>
 *   <li><b>支持链式异常：</b>可以包含原始异常 {@code cause}，保留完整的异常堆栈信息，便于问题溯源</li>
 *   <li><b>序列化支持：</b>实现 {@link java.io.Serializable} 接口，支持在分布式系统中跨网络传输异常</li>
 *   <li><b>运行时异常：</b>继承 {@link RuntimeException}，无需在方法签名中显式声明，减少代码侵入</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li><b>统一基类：</b>为所有业务异常提供统一的基类，便于全局异常处理器统一捕获和处理</li>
 *   <li><b>强制错误码：</b>通过构造方法强制子类必须提供错误码，确保每个异常都有明确的业务含义</li>
 *   <li><b>异常体系一致性：</b>确保整个应用中的业务异常都遵循相同的结构和行为，提高代码可维护性</li>
 *   <li><b>与全局异常处理器集成：</b>与 {@link com.sandbox.services.common.base.handler.GlobalExceptionHandler}
 *       配合，实现错误码和错误信息的标准化输出</li>
 * </ul>
 *
 * <p><b>异常结构说明：</b>
 * <ul>
 *   <li><b>code：</b>业务错误码，用于标识具体的错误类型，通常与 {@link com.sandbox.services.common.base.enumeration.ResponseCodeEnum}
 *       中的定义对应</li>
 *   <li><b>message：</b>错误描述信息，提供人类可读的错误详情，可直接展示给前端用户</li>
 *   <li><b>cause：</b>原始异常原因，用于保留底层异常的堆栈信息</li>
 * </ul>
 *
 * <p><b>使用方式：</b>
 * 此类为抽象类，不应直接实例化。具体的业务异常应继承此类，并提供相应的构造方法。
 * 例如：
 * <pre>
 * public class BusinessException extends AbstractException {
 *     public BusinessException(ResponseCodeEnum responseCodeEnum) {
 *         super(responseCodeEnum.getCode(), responseCodeEnum.getDescription());
 *     }
 * }
 * </pre>
 *
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>所有构造方法均为 {@code protected}，限制外部直接调用，强制子类提供适当的构造方法</li>
 *   <li>错误码 {@code code} 一旦创建不可修改，通过 Lombok 的 {@code @Getter} 注解提供访问方法</li>
 *   <li>序列化版本号 {@code serialVersionUID} 使用 {@code @Serial} 注解标记，确保版本兼容性</li>
 *   <li>由于继承 {@link RuntimeException}，异常抛出时不会强制调用方进行 try-catch 处理</li>
 * </ul>
 *
 * @author 0101
 * @see com.sandbox.services.common.base.exception.BusinessException
 * @see com.sandbox.services.common.base.handler.GlobalExceptionHandler
 * @see com.sandbox.services.common.base.enumeration.ResponseCodeEnum
 * @since 2026-03-12
 */
@Getter
public abstract class AbstractException extends RuntimeException {

    /**
     * 序列化版本号
     *
     * <p>用于确保在不同版本的 JVM 中序列化和反序列化的兼容性。
     * 当类的结构发生变化时，需要更新此版本号。
     */
    @Serial
    private static final long serialVersionUID = 803863908956713716L;

    /**
     * 业务错误码
     *
     * <p>用于标识具体的错误类型，与 {@link com.sandbox.services.common.base.enumeration.ResponseCodeEnum}
     * 中的定义对应。通过错误码，前端可以精确判断错误类型并做出相应处理。
     */
    private final int code;

    /**
     * 构造包含错误码和错误消息的异常
     *
     * @param code    业务错误码
     * @param message 错误描述信息
     */
    protected AbstractException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造包含错误码和原始异常的异常
     *
     * <p>适用于由其他异常引发的业务异常，保留原始异常的堆栈信息。
     *
     * @param code  业务错误码
     * @param cause 原始异常原因
     */
    protected AbstractException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    /**
     * 构造包含错误码、错误消息和原始异常的异常
     *
     * <p>提供最完整的异常信息，包含错误码、自定义错误描述以及原始异常原因。
     *
     * @param code    业务错误码
     * @param message 错误描述信息
     * @param cause   原始异常原因
     */
    protected AbstractException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}