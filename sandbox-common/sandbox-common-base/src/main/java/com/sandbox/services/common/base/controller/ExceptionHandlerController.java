package com.sandbox.services.common.base.controller;

import com.sandbox.services.common.base.enumeration.ResponseCodeEnum;
import com.sandbox.services.common.base.exception.BusinessException;
import com.sandbox.services.common.base.vo.R;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * @description: 全局异常捕获
 * @author: 0101
 * @create: 2026/3/12
 */
@RestControllerAdvice
public class ExceptionHandlerController {

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerController.class);

    public ExceptionHandlerController() {
    }

    @ExceptionHandler({BusinessException.class})
    public ResponseEntity<R<?>> handler(BusinessException e) {
        log.error("业务异常：msg = {} ", e.getMessage(), e);
        return new ResponseEntity<>(new R<>(e.getCode(), e.getMessage()), HttpStatus.OK);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<R<?>> handleRuntimeException(RuntimeException e) {
        return new ResponseEntity<>(new R<>(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 捕获请求体参数校验异常（@RequestBody + @Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<?>> handleValidationException(MethodArgumentNotValidException e) {
        log.error("参数异常：msg = {} ", e.getMessage(), e);
        String errorMsg = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(";"));
        return new ResponseEntity<>(new R<>(ResponseCodeEnum.PARAMETER_ERROR.getValue(), errorMsg), HttpStatus.OK);
    }

    /**
     * 捕获路径/查询参数校验异常（@RequestParam/@PathVariable + @Validated）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<R<?>> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("参数异常：msg = {} ", e.getMessage(), e);
        return new ResponseEntity<>(new R<>(ResponseCodeEnum.PARAMETER_ERROR.getValue(), e.getMessage()), HttpStatus.OK);
    }

    /**
     * 请求方式错误异常捕获
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<R<?>> handleValidationException(HttpRequestMethodNotSupportedException e) {
        log.error("请求方式异常：msg = {} ", e.getMessage(), e);
        return new ResponseEntity<>(new R<>(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION.getValue(), e.getMessage()), HttpStatus.OK);
    }
}
