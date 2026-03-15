package com.sandbox.services.living.enumeration;

import lombok.Getter;

/**
 * @description:
 * @author: 0101
 * @create: 2026/3/12
 */
@Getter
public enum LivingResponseCodeEnum {

    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403,  "Forbidden"),

    ;

    private final int code;
    private final String description;

    private LivingResponseCodeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

}
