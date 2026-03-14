package com.sandbox.services.living.model.vo.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * @description: 登录响应
 * @author: 0101
 * @create: 2026/03/14
 */
@Data
@AllArgsConstructor
public class LoginVO {

    private String token;
    private String username;
    private Date expireTime;
}
