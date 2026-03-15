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
 * 集中处理应用程序中的各种异常，并返回标准化的响应格式
 *
 * @author 0101
 * @since 2026-03-12
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     *
     * @param e BusinessException 实例
     * @return 包含错误码和错误信息的响应实体
     */
    @ExceptionHandler({BusinessException.class})
    public ResponseEntity<R<?>> handler(BusinessException e) {
        log.error("Business exception occurred: code = {}, message = {}", e.getCode(), e.getMessage(), e);
        return new ResponseEntity<>(new R<>(e.getCode(), e.getMessage()), HttpStatus.OK);
    }

    /**
     * 处理通用运行时异常
     *
     * @param e RuntimeException 实例
     * @return 包含内部服务器错误状态的响应实体
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<R<?>> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception occurred: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new R<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    /**
     * 处理 404 Not Found 异常（找不到请求处理器）
     *
     * @param e NoHandlerFoundException 实例
     * @return 包含 404 状态和错误详情的响应实体
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<R<?>> handleNoHandlerFound(NoHandlerFoundException e) {
        String requestInfo = String.format("%s %s", e.getHttpMethod(), e.getRequestURL());
        log.warn("404 Not Found: {}", requestInfo);
        return new ResponseEntity<>(
                new R<>(HttpStatus.NOT_FOUND.value(), "Not Found: " + requestInfo),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * 处理请求体参数校验异常（@RequestBody + @Valid）
     * 收集所有校验错误信息并以分号分隔返回
     *
     * @param e MethodArgumentNotValidException 实例
     * @return 包含参数错误码和校验信息的响应实体
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<?>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.error("Request body validation failed: {}", errorMsg, e);
        return new ResponseEntity<>(
                new R<>(ResponseCodeEnum.PARAMETER_ERROR.getCode(), errorMsg),
                HttpStatus.OK
        );
    }

    /**
     * 处理路径/查询参数校验异常（@RequestParam/@PathVariable + @Validated）
     *
     * @param e ConstraintViolationException 实例
     * @return 包含参数错误码和校验信息的响应实体
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<R<?>> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("Parameter validation failed: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new R<>(ResponseCodeEnum.PARAMETER_ERROR.getCode(), e.getMessage()),
                HttpStatus.OK
        );
    }

    /**
     * 处理 HTTP 请求方式不支持异常
     *
     * @param e HttpRequestMethodNotSupportedException 实例
     * @return 包含请求错误码和错误信息的响应实体
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<R<?>> handleValidationException(HttpRequestMethodNotSupportedException e) {
        log.error("HTTP method not supported: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new R<>(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION.getCode(), e.getMessage()),
                HttpStatus.OK
        );
    }
}