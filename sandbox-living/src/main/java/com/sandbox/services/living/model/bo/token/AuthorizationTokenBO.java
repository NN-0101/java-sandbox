package com.sandbox.services.living.model.bo.token;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自定义令牌实体
 *
 * <p>封装用户认证令牌的完整信息，用于在 {@link com.sandbox.services.living.security.manager.TokenManager}
 * 中存储和管理令牌数据。该实体包含了令牌的所有元数据信息。</p>
 *
 * <p><b>字段说明：</b></p>
 * <ul>
 *   <li><b>tokenValue：</b>令牌的唯一标识值，由 UUID 生成，作为存储和查找的键</li>
 *   <li><b>userId：</b>用户唯一标识，用于关联具体用户</li>
 *   <li><b>username：</b>用户名/手机号，用于日志和调试</li>
 *   <li><b>createTime：</b>令牌创建时间，用于计算存活时间</li>
 *   <li><b>expireTime：</b>令牌过期时间，超过此时间令牌自动失效</li>
 *   <li><b>isValid：</b>令牌是否有效，支持管理员手动使令牌失效（如用户修改密码后）</li>
 * </ul>
 *
 * <p><b>令牌生命周期：</b></p>
 * <ol>
 *   <li><b>创建：</b>用户登录成功时生成，设置创建时间和过期时间（默认 120 分钟）</li>
 *   <li><b>验证：</b>每次请求时检查是否有效且未过期</li>
 *   <li><b>刷新：</b>旧令牌有效时可换取新令牌，延长登录状态</li>
 *   <li><b>销毁：</b>用户登出或令牌过期/失效时从存储中移除</li>
 * </ol>
 *
 * <p><b>存储建议：</b></p>
 * <ul>
 *   <li>开发环境：可使用内存存储（ConcurrentHashMap）</li>
 *   <li>生产环境：建议使用 Redis，设置 key 为 "token:" + tokenValue，并利用 Redis 的 TTL 功能</li>
 * </ul>
 *
 * @author 0101
 * @since 2026-03-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationTokenBO {

    /**
     * accessToken
     */
    private String accessToken;

    /**
     * accessToken值
     */
    private AccessTokenValueBO accessTokenValue;

    /**
     * refreshToken
     */
    private String refreshToken;

    /**
     * refreshToken值
     */
    private RefreshTokenValueBO refreshTokenValue;
}
