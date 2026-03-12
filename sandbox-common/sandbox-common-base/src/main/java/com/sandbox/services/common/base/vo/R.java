package com.sandbox.services.common.base.vo;

import com.sandbox.services.common.base.enumeration.ResponseCodeEnum;
import com.yomahub.tlog.context.TLogContext;
import lombok.Getter;
import lombok.Setter;

/**
 * @description:
 * @author: xp
 * @create: 2025/3/7
 */
@Getter
@Setter
public class R<T> {

    private String code;

    private String msg;

    private T data;

    private final String traceId = TLogContext.getTraceId();


    public R() {
    }

    public R(ResponseCodeEnum responseCodeEnum) {
        this.setCode(responseCodeEnum.getValue());
        this.setMsg(responseCodeEnum.getDescription());
    }

    public R(String code, String message){
        this.setCode(code);
        this.setMsg(message);
    }

    public static <T> R<T> success(T data) {
        R<T> genericResponse = new R<>(ResponseCodeEnum.SUCCESS);
        genericResponse.setData(data);
        return genericResponse;
    }

    public static <T> R<T> success() {
        return new R(ResponseCodeEnum.SUCCESS);
    }

    public static <T> R<T> fail(ResponseCodeEnum responseCode, String msg) {
        R<T> r = new R<>(responseCode);
        r.setMsg(msg);
        return r;
    }

    public static <T> R<T> fail(ResponseCodeEnum responseCode, T data) {
        R<T> r = new R<>(responseCode);
        r.setData(data);
        return r;
    }

    public static <T> R<T> fail(String code, String msg, T data) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    public static <T> R<T> fail(String code, String msg, String eid) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    public Boolean equalsSuccess() {
        return ResponseCodeEnum.SUCCESS.getValue().equals(this.code);
    }

}
