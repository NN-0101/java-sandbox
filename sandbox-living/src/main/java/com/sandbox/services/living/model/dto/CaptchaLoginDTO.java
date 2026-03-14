package com.sandbox.services.living.model.dto;

import lombok.Data;

/**
 * @description: 验证码登录参数
 * @author: 0101
 * @create: 2026/03/14
 */
@Data
public class CaptchaLoginDTO {

    /**
     * 手机号或邮箱
     */
    private String account;

    /**
     * 验证码
     */
    private String captcha;
}
