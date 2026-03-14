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
 * @description: 令牌管理器
 * @author: 0101
 * @create: 2026/03/14
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
