package com.sandbox.services.common.base.exception;

import lombok.Getter;

import java.io.Serial;

/**
 * @description: * 抽象业务异常 - 所有业务异常的基类
 * *
 * * 核心特性：
 * * 1. 统一异常结构：所有业务异常都包含code和message
 * * 2. 支持链式异常：可以包含原始异常cause
 * * 3. 序列化支持：实现Serializable接口
 * *
 * * 设计目的：
 * * - 为所有业务异常提供统一的基类
 * * - 强制子类必须提供错误码
 * * - 确保异常体系的一致性
 * @author: 0101
 * @create: 2026/3/12
 */
@Getter
public class AbstractException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 803863908956713716L;

    private final int code;

    protected AbstractException(int code, String message) {
        super(message);
        this.code = code;
    }

    protected AbstractException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    protected AbstractException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}
