package com.sandbox.services.living.security.token;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @description: 自定义令牌实体
 * @author: 0101
 * @create: 2026/03/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomToken {

    /**
     * 令牌值
     */
    private String tokenValue;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 是否有效
     */
    private Boolean isValid;
}
