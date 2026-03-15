package com.sandbox.services.living.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @description: 登录请求参数
 * @author: 0101
 * @create: 2026/03/14
 */
@Data
public class LoginDTO {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;
}
