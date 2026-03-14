package com.sandbox.services.living.model.dto.auth;

import lombok.Data;

/**
 * @description: 登录请求参数
 * @author: 0101
 * @create: 2026/03/14
 */
@Data
public class LoginDTO {
    private String username;

    private String password;
}
