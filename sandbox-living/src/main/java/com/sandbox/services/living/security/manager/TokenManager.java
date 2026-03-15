package com.sandbox.services.living.security.manager;

import com.sandbox.services.living.security.token.CustomToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 令牌管理器
 *
 * <p>负责用户认证令牌的完整生命周期管理，包括令牌的创建、验证、刷新和销毁。
 * 当前使用内存存储（ConcurrentHashMap），生产环境建议替换为 Redis 实现分布式存储。</p>
 *
 * <p><b>核心功能：</b></p>
 * <ul>
 *   <li><b>令牌创建：</b>用户登录成功后生成唯一的认证令牌</li>
 *   <li><b>令牌验证：</b>校验令牌的有效性、过期时间和状态</li>
 *   <li><b>令牌刷新：</b>在旧令牌有效时颁发新令牌，实现无感续期</li>
 *   <li><b>令牌销毁：</b>用户登出时主动使令牌失效</li>
 * </ul>
 *
 * <p><b>令牌存储结构：</b></p>
 * <pre>
 * tokenStore = {
 *     "7d8f3e2a1b9c4d5e6f7a8b9c0d1e2f3a": CustomToken {
 *         tokenValue: "7d8f3e2a1b9c4d5e6f7a8b9c0d1e2f3a",
 *         userId: "438929034284234224",
 *         username: "186****5429",
 *         createTime: 2026-03-15 10:30:00,
 *         expireTime: 2026-03-15 12:30:00,
 *         isValid: true
 *     }
 * }
 * </pre>
 *
 * <p><b>注意事项：</b></p>
 * <ul>
 *   <li>令牌默认有效期 120 分钟（2 小时）</li>
 *   <li>使用 UUID 生成 32 位无分隔符的令牌字符串</li>
 *   <li>令牌验证时会自动清理已过期或失效的令牌</li>
 *   <li>多实例部署时必须使用共享存储（如 Redis）替代内存存储</li>
 * </ul>
 *
 * @author 0101
 * @see CustomToken
 * @see com.sandbox.services.living.security.filter.CustomAuthenticationFilter
 * @since 2026-03-14
 */
@Slf4j
@Component
public class TokenManager {

    // 内存存储令牌，生产环境建议使用Redis
    private final Map<String, CustomToken> tokenStore = new ConcurrentHashMap<>();

    // 令牌过期时间（分钟）
    private static final int TOKEN_EXPIRE_MINUTES = 120;

    /**
     * 创建令牌
     */
    public CustomToken createToken(String userId, String username) {
        String tokenValue = generateTokenValue();

        Date now = new Date();  // 当前时间

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, TOKEN_EXPIRE_MINUTES);
        Date expireDate = calendar.getTime();

        CustomToken token = new CustomToken(
                tokenValue,
                userId,
                username,
                new Date(),
                expireDate,
                true
        );

        tokenStore.put(tokenValue, token);
        log.info("创建令牌: userId={}, username={}, token={}", userId, username, tokenValue);

        return token;
    }

    /**
     * 验证令牌
     */
    public CustomToken validateToken(String tokenValue) {
        if (tokenValue == null || tokenValue.isEmpty()) {
            return null;
        }

        CustomToken token = tokenStore.get(tokenValue);

        if (token == null) {
            log.warn("令牌不存在: {}", tokenValue);
            return null;
        }

        // 检查令牌是否有效
        if (!token.getIsValid()) {
            log.warn("令牌已失效: {}", tokenValue);
            tokenStore.remove(tokenValue);
            return null;
        }

        // 检查令牌是否过期
        if (token.getExpireTime().before(new Date())) {
            log.warn("令牌已过期: {}", tokenValue);
            tokenStore.remove(tokenValue);
            return null;
        }

        return token;
    }

    /**
     * 刷新令牌
     */
    public CustomToken refreshToken(String tokenValue) {
        CustomToken oldToken = validateToken(tokenValue);
        if (oldToken == null) {
            return null;
        }

        // 移除旧令牌
        tokenStore.remove(tokenValue);

        // 创建新令牌
        return createToken(oldToken.getUserId(), oldToken.getUsername());
    }

    /**
     * 移除令牌（登出）
     */
    public boolean removeToken(String tokenValue) {
        CustomToken removed = tokenStore.remove(tokenValue);
        if (removed != null) {
            log.info("令牌已移除: {}", tokenValue);
            return true;
        }
        return false;
    }

    /**
     * 生成令牌值
     */
    private String generateTokenValue() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
