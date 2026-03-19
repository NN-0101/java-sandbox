package com.sandbox.services.living.security.token.model;

import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author: 0101
 * @create: 2026/03/15
 */
@Data
public class RefreshTokenValueBO {

    private String userId;

    private String phone;

    private Date expireTime;

    private String accessToken;
}
