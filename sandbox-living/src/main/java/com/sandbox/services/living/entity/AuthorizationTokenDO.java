package com.sandbox.services.living.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.sandbox.services.db.mysql.model.BaseModel;
import java.io.Serial;

/**
 * @description: 授权token表(AuthorizationToken)表数据库实体
 * @author: 0101
 * @create: 2026-03-15 20:16:17
 */
@Data
@TableName("t_authorization_token")
@EqualsAndHashCode(callSuper = true)
public class AuthorizationTokenDO extends BaseModel<AuthorizationTokenDO> {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * accessToken
     */
    private String accessToken;
     
    /**
     * accessToken值
     */
    private String accessTokenValue;
     
    /**
     * refreshToken
     */
    private String refreshToken;
     
    /**
     * refreshToken值
     */
    private String refreshTokenValue;
     

}
