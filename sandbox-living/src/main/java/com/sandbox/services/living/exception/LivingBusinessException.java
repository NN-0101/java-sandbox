package com.sandbox.services.living.exception;


import com.sandbox.services.common.base.exception.AbstractException;
import com.sandbox.services.living.enumeration.LivingResponseCodeEnum;

/**
 * @description: Living模块业务异常 - 表示业务逻辑处理过程中的异常
 * @author: 0101
 * @create: 2026/3/12
 */
public class LivingBusinessException extends AbstractException {

    public LivingBusinessException(LivingResponseCodeEnum responseCodeEnum) {
        super(responseCodeEnum.getCode(), responseCodeEnum.getDescription());
    }

    public LivingBusinessException(LivingResponseCodeEnum responseCodeEnum, Throwable cause) {
        super(responseCodeEnum.getCode(), responseCodeEnum.getDescription(), cause);
    }

    public LivingBusinessException(int code, String message) {
        super(code, message);
    }

    protected LivingBusinessException(int code, Throwable cause) {
        super(code, cause);
    }

    protected LivingBusinessException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
