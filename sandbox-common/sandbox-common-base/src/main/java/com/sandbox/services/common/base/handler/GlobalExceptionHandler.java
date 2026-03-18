package com.sandbox.services.common.base.handler;

import com.sandbox.services.common.base.enumeration.ResponseCodeEnum;
import com.sandbox.services.common.base.exception.BusinessException;
import com.sandbox.services.common.base.vo.R;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常捕获处理器
 *
 * <p>该类使用 {@link RestControllerAdvice} 注解，作为 Spring Boot 应用的全局异常处理中心，
 * 集中处理应用程序中抛出的各种异常，并将异常信息转换为统一的响应格式 {@link R} 返回给客户端。
 * 通过这种方式，可以确保所有接口的异常响应格式一致，便于前端统一处理和展示错误信息。
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>业务异常处理：</b>捕获自定义的 {@link BusinessException}，返回业务错误码和提示信息</li>
 *   <li><b>参数校验异常：</b>处理 {@link MethodArgumentNotValidException}（请求体校验）和
 *       {@link ConstraintViolationException}（路径/查询参数校验）</li>
 *   <li><b>运行时异常：</b>捕获所有未预期的 {@link RuntimeException}，返回 500 内部服务器错误</li>
 *   <li><b>404 异常：</b>处理 {@link NoHandlerFoundException}，返回友好的 404 提示</li>
 *   <li><b>请求方式异常：</b>处理 {@link HttpRequestMethodNotSupportedException}，提示请求方法不支持</li>
 *   <li><b>日志记录：</b>所有异常都会记录详细的错误日志，便于问题排查</li>
 * </ul>
 *
 * <p><b>异常处理策略：</b>
 * <table border="1">
 *   <tr>
 *     <th>异常类型</th>
 *     <th>HTTP 状态码</th>
 *     <th>返回码</th>
 *     <th>说明</th>
 *   </tr>
 *   <tr>
 *     <td>{@link BusinessException}</td>
 *     <td>200 OK</td>
 *     <td>业务自定义</td>
 *     <td>业务异常，由业务逻辑主动抛出，返回码由业务定义</td>
 *   </tr>
 *   <tr>
 *     <td>{@link MethodArgumentNotValidException}<br>{@link ConstraintViolationException}</td>
 *     <td>200 OK</td>
 *     <td>{@link ResponseCodeEnum#PARAMETER_ERROR} (1002)</td>
 *     <td>参数校验失败，返回具体的校验错误信息</td>
 *   </tr>
 *   <tr>
 *     <td>{@link HttpRequestMethodNotSupportedException}</td>
 *     <td>200 OK</td>
 *     <td>{@link ResponseCodeEnum#HTTP_REQUEST_EXCEPTION} (1004)</td>
 *     <td>HTTP 请求方式不支持</td>
 *   </tr>
 *   <tr>
 *     <td>{@link NoHandlerFoundException}</td>
 *     <td>404 Not Found</td>
 *     <td>404</td>
 *     <td>请求的接口不存在</td>
 *   </tr>
 *   <tr>
 *     <td>{@link RuntimeException}</td>
 *     <td>500 Internal Server Error</td>
 *     <td>500</td>
 *     <td>未预期的运行时异常</td>
 *   </tr>
 * </table>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>统一响应格式：</b>所有异常都包装为 {@link R} 对象，保持响应格式一致性</li>
 *   <li><b>详细的日志记录：</b>每个异常都记录错误日志，包含堆栈信息，便于问题定位</li>
 *   <li><b>参数校验优化：</b>对于 {@link MethodArgumentNotValidException}，收集所有校验错误并用分号拼接，
 *       一次性返回全部错误信息，减少客户端调用次数</li>
 *   <li><b>业务异常处理：</b>业务异常返回 HTTP 200 状态码，通过业务码区分错误类型，便于前端统一处理</li>
 *   <li><b>404 友好提示：</b>对不存在的接口返回友好的提示信息，包含请求方法和 URL</li>
 * </ul>
 *
 * <p><b>配置要求：</b>
 * 为了使 {@link NoHandlerFoundException} 生效，需要在 application.yml 中配置：
 * <pre>
 * spring:
 *   mvc:
 *     throw-exception-if-no-handler-found: true
 *   web:
 *     resources:
 *       add-mappings: false
 * </pre>
 *
 * @author 0101
 * @see RestControllerAdvice
 * @see BusinessException
 * @see R
 * @see ResponseCodeEnum
 * @since 2026-03-12
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     *
     * <p>当业务逻辑主动抛出 {@link BusinessException} 时，此方法负责捕获并返回标准化的错误响应。
     * 业务异常通常表示可预料的业务规则违反（如余额不足、用户不存在等）。
     *
     * @param e BusinessException 实例，包含错误码和错误信息
     * @return 包含错误码和错误信息的响应实体，HTTP 状态码为 200
     */
    @ExceptionHandler({BusinessException.class})
    public ResponseEntity<R<?>> handler(BusinessException e) {
        log.error("业务异常: code = {}, message = {}", e.getCode(), e.getMessage(), e);
        return new ResponseEntity<>(new R<>(e.getCode(), e.getMessage()), HttpStatus.OK);
    }

    /**
     * 处理通用运行时异常
     *
     * <p>捕获所有未预期的 {@link RuntimeException}，这通常是系统内部错误或未处理的其他异常。
     * 返回 HTTP 500 状态码，表示服务器内部错误。
     *
     * @param e RuntimeException 实例
     * @return 包含内部服务器错误状态的响应实体，HTTP 状态码为 500
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<R<?>> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new R<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    /**
     * 处理 404 Not Found 异常（找不到请求处理器）
     *
     * <p>当客户端请求的 URL 不存在时，Spring MVC 会抛出 {@link NoHandlerFoundException}。
     * 此方法返回友好的 404 提示，包含请求方法和 URL。
     *
     * @param e NoHandlerFoundException 实例
     * @return 包含 404 状态和错误详情的响应实体，HTTP 状态码为 404
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<R<?>> handleNoHandlerFound(NoHandlerFoundException e) {
        String requestInfo = String.format("%s %s", e.getHttpMethod(), e.getRequestURL());
        log.warn("404 未找到: {}", requestInfo);
        return new ResponseEntity<>(
                new R<>(HttpStatus.NOT_FOUND.value(), "接口不存在: " + requestInfo),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * 处理请求体参数校验异常
     *
     * <p>当使用 {@code @RequestBody} 接收参数并配合 {@code @Valid} 注解进行校验时，
     * 如果校验失败，会抛出 {@link MethodArgumentNotValidException}。
     * 该方法收集所有校验错误信息，用分号拼接后返回。
     *
     * @param e MethodArgumentNotValidException 实例
     * @return 包含参数错误码和校验信息的响应实体，HTTP 状态码为 200
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<?>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.error("请求体参数校验失败: {}", errorMsg, e);
        return new ResponseEntity<>(
                new R<>(ResponseCodeEnum.PARAMETER_ERROR.getCode(), errorMsg),
                HttpStatus.OK
        );
    }

    /**
     * 处理路径/查询参数校验异常
     *
     * <p>当使用 {@code @RequestParam}、{@code @PathVariable} 配合 {@code @Validated} 注解
     * 进行参数校验时，如果校验失败，会抛出 {@link ConstraintViolationException}。
     *
     * @param e ConstraintViolationException 实例
     * @return 包含参数错误码和校验信息的响应实体，HTTP 状态码为 200
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<R<?>> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("参数校验失败: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new R<>(ResponseCodeEnum.PARAMETER_ERROR.getCode(), e.getMessage()),
                HttpStatus.OK
        );
    }

    /**
     * 处理 HTTP 请求方式不支持异常
     *
     * <p>当客户端使用错误的 HTTP 方法访问接口时（如用 POST 访问 GET 接口），
     * Spring MVC 会抛出 {@link HttpRequestMethodNotSupportedException}。
     *
     * @param e HttpRequestMethodNotSupportedException 实例
     * @return 包含请求错误码和错误信息的响应实体，HTTP 状态码为 200
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<R<?>> handleHttpMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.error("HTTP 请求方式不支持: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new R<>(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION.getCode(), e.getMessage()),
                HttpStatus.OK
        );
    }
}