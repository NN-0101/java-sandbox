package com.sandbox.services.common.base.exception;


import com.sandbox.services.common.base.enumeration.ResponseCodeEnum;

/**
 * @description: * 业务异常 - 表示业务逻辑处理过程中的异常
 * *
 * * 使用场景：
 * * - 参数校验失败
 * * - 业务规则违反（如：余额不足、库存不够）
 * * - 资源不存在或已存在
 * * - 权限不足
 * * - 状态不允许操作
 * *
 * * 设计特点：
 * * 1. 继承AbstractException，复用基础结构
 * * 2. 与ResponseCodeEnum枚举紧密集成
 * * 3. 提供多种构造方式，适应不同场景
 * @author: 0101
 * @create: 2026/3/12
 */
public class BusinessException extends AbstractException {

    public BusinessException(ResponseCodeEnum responseCodeEnum) {
        super(responseCodeEnum.getValue(), responseCodeEnum.getDescription());
    }

    public BusinessException(ResponseCodeEnum responseCodeEnum, Throwable cause) {
        super(responseCodeEnum.getValue(), responseCodeEnum.getDescription(), cause);
    }

    protected BusinessException(String code, String message) {
        super(code, message);
    }

    protected BusinessException(String code, Throwable cause) {
        super(code, cause);
    }

    protected BusinessException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
